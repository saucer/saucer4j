package co.casterlabs.saucer.impl;

import java.util.function.BiFunction;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import co.casterlabs.saucer.documentation.PointerType;
import co.casterlabs.saucer.impl.SaucerSerializer._Native.FunctionCallback;
import co.casterlabs.saucer.impl.SaucerSerializer._Native.ParserCallback;
import lombok.NonNull;

@SuppressWarnings("deprecation")
@PointerType
class SaucerSerializer extends _SaucerPointer {
    private static final _Native N = _SaucerNative.load(_Native.class);

    private BiFunction<String, JsonArray, JsonElement> messageHandler;

    private ParserCallback parserCallback = new ParserCallback() {
        @Override
        public Pointer callback(Pointer $rawMessage) {
            try {
                String rawMessage = $rawMessage.getString(0);
                JsonObject message = Rson.DEFAULT.fromJson(rawMessage, JsonObject.class);

                if (message.containsKey("saucer:call") && message.getBoolean("saucer:call")) {
                    long id = message.getNumber("id").longValue();
                    String name = message.getString("name");
                    Pointer $rawMessageCopy = N.saucer_strdup(rawMessage);

                    return N.saucer_function_data_new(id, name, $rawMessageCopy);
                }
            } catch (Throwable t) {
                System.err.println("Error whilst handling message:");
                t.printStackTrace();
            }

            return Pointer.NULL;
        }
    };

    private FunctionCallback functionCallback = new FunctionCallback() {
        @Override
        public void callback(Pointer $functionData, Pointer $resolver, Pointer $rejecter) {
            try {
                Pointer $userData = N.saucer_function_data_get_user_data($functionData);
                String rawMessage = $userData.getString(0);
                N.saucer_alloc_free($userData);

                JsonObject message = Rson.DEFAULT.fromJson(rawMessage, JsonObject.class);

                JsonElement result = messageHandler.apply(message.getString("name"), message.getArray("params"));
                if (result == null) {
                    // Note that result being null is different from a JsonNull. We treat this as
                    // undefined.
                    N.saucer_serializer_resolve($resolver, "undefined");
                } else {
                    N.saucer_serializer_resolve($resolver, result.toString());
                }
            } catch (Throwable t) {
                String reason = SaucerUtils.getExceptionStack(t);
                System.err.printf("An error occurred whilst processing function, bubbling to JavaScript.\n%s\n", reason);
                N.saucer_serializer_reject($rejecter, new JsonString(reason).toString());
            }
        }
    };

    public SaucerSerializer(@NonNull BiFunction<String, JsonArray, JsonElement> messageHandler) {
        this.messageHandler = messageHandler;

        this.setup(N.saucer_serializer_new(), (noop) -> {
            // TODO curve may need to implement a free() for us.
        });

        N.saucer_serializer_set_js_serializer(this.p(), "JSON.stringify");
        N.saucer_serializer_set_parser(this.p(), this.parserCallback);
        N.saucer_serializer_set_function(this.p(), this.functionCallback);
    }

    // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/memory.h
    // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/serializer.h
    static interface _Native extends Library {

        void saucer_alloc_free(Pointer $ptr);

        Pointer saucer_strdup(String in);

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface ParserCallback extends Callback {

            Pointer callback(Pointer $rawMessage);

        }

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface FunctionCallback extends Callback {

            /**
             * @param $functionData call
             *                      {@link _SaucerNative#saucer_function_data_get_user_data(Pointer)};
             */
            void callback(Pointer $functionData, Pointer $resolver, Pointer $rejecter);

        }

        Pointer saucer_serializer_new();

        void saucer_serializer_set_js_serializer(Pointer $serializer, String jsSerializer);

        void saucer_serializer_set_parser(Pointer $serializer, ParserCallback cSerializer);

        Pointer saucer_function_data_new(long id, String name, Pointer userData);

        void saucer_serializer_set_function(Pointer $serializer, FunctionCallback func);

        Pointer saucer_function_data_get_user_data(Pointer $functionData);

        void saucer_serializer_resolve(Pointer $function, String result);

        void saucer_serializer_reject(Pointer $function, String reason);

    }

}
