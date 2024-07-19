package co.casterlabs.saucer.natives;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import co.casterlabs.saucer.SaucerBridge;
import co.casterlabs.saucer.documentation.PointerType;
import co.casterlabs.saucer.natives.ImplSaucerBridge._Native.FunctionCallback;
import co.casterlabs.saucer.natives.ImplSaucerBridge._Native.ParserCallback;
import lombok.NonNull;

@SuppressWarnings("deprecation")
@PointerType
class ImplSaucerBridge extends _SafePointer implements SaucerBridge {
    private static final _Native N = _SaucerNative.load(_Native.class);

    // TODO this entire class. LMAO

    private ParserCallback parserCallback = (Pointer $rawMessage) -> {
        try {
            String rawMessage = $rawMessage.getString(0);
            JsonObject message = Rson.DEFAULT.fromJson(rawMessage, JsonObject.class);

            if (message.containsKey("saucer:call") && message.getBoolean("saucer:call")) {
                long id = message.getNumber("id").longValue();
                String name = message.getString("name");
                Pointer $rawMessageCopy = N.saucer_strdup(rawMessage); // deallocated later!!!

                return N.saucer_function_data_new(id, name, $rawMessageCopy);
            }
        } catch (Throwable t) {
            System.err.println("Error whilst handling message:");
            t.printStackTrace();
        }

        return Pointer.NULL;
    };

    private FunctionCallback functionCallback = (Pointer $functionData, Pointer $resolver, Pointer $rejecter) -> {
        try {
            _SafePointer $userData = _SafePointer.of(N.saucer_function_data_get_user_data($functionData)); // here's our deallocate ;)
            String rawMessage = $userData.p().getString(0);

            JsonObject message = Rson.DEFAULT.fromJson(rawMessage, JsonObject.class);

            throw new IllegalStateException("TODO");
//                JsonElement result = messageHandler.apply(message.getString("name"), message.getArray("params"));
//                if (result == null) {
//                    // Note that result being null is different from a JsonNull. We treat this as
//                    // undefined.
//                    N.saucer_serializer_resolve($resolver, "undefined");
//                } else {
//                    N.saucer_serializer_resolve($resolver, result.toString());
//                }
        } catch (Throwable t) {
            String reason = getExceptionStack(t);
            System.err.printf("An error occurred whilst processing function, bubbling to JavaScript.\n%s\n", reason);
            N.saucer_serializer_reject($rejecter, new JsonString(reason).toString());
        }
    };

    ImplSaucerBridge() {
        this.setup(N.saucer_serializer_new(), (noop) -> {
            // NO-OP
        });

        N.saucer_serializer_set_js_serializer(this.p(), "JSON.stringify");
        N.saucer_serializer_set_parser(this.p(), this.parserCallback);
        N.saucer_serializer_set_function(this.p(), this.functionCallback);
    }

    static interface _Native extends Library {

        /* ---------------------------- */
        // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/window.h
        /* ---------------------------- */

        // TODO implement these.
        void saucer_window_start_drag(Pointer $saucer);

        void saucer_window_start_resize(Pointer $saucer, int edge);

        /* ---------------------------- */
        // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/memory.h
        /* ---------------------------- */

        void saucer_alloc_free(Pointer $ptr);

        Pointer saucer_strdup(String in);

        /* ---------------------------- */
        // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/serializer.h
        /* ---------------------------- */

        static final int SAUCER_LAUNCH_POLICY_SYNC = 0;
//      static final int SAUCER_LAUNCH_POLICY_ASYNC = 1;

        void saucer_add_function(Pointer $saucer, String name, int saucerLaunchPolicy);

        Pointer saucer_serializer_new();

        void saucer_serializer_set_js_serializer(Pointer $serializer, String jsSerializer);

        void saucer_serializer_set_parser(Pointer $serializer, ParserCallback cSerializer);

        Pointer saucer_function_data_new(long id, String name, Pointer userData);

        void saucer_serializer_set_function(Pointer $serializer, FunctionCallback func);

        Pointer saucer_function_data_get_user_data(Pointer $functionData);

        void saucer_serializer_resolve(Pointer $function, String result);

        void saucer_serializer_reject(Pointer $function, String reason);

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

    }

    private static String getExceptionStack(@NonNull Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        e.printStackTrace(pw);

        String out = sw.toString();

        pw.flush();
        pw.close();
        sw.flush();

        return out
            .substring(0, out.length() - 2)
            .replace("\r", "");
    }

}
