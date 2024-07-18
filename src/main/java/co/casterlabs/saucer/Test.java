package co.casterlabs.saucer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.sun.jna.Pointer;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.saucer._SaucerNative.SerializerCallback;
import co.casterlabs.saucer._SaucerNative.SerializerFunction;
import lombok.NonNull;
import lombok.SneakyThrows;

public class Test {

    private static SerializerCallback parserCallback = new SerializerCallback() {
        @Override
        public Pointer callback(Pointer $rawMessage) {
            try {
                String rawMessage = $rawMessage.getString(0);

                System.err.println(rawMessage);
                JsonObject message = Rson.DEFAULT.fromJson(rawMessage, JsonObject.class);
                System.err.println("POINT ALPHA");

                if (message.containsKey("saucer:call") && message.getBoolean("saucer:call")) {
                    long id = message.getNumber("id").longValue();
                    String name = message.getString("name");
                    Pointer $rawMessageCopy = _SaucerNative.N.saucer_strdup(rawMessage);

                    System.err.println("POINT BETA");
                    return _SaucerNative.N.saucer_function_data_new(
                        id,
                        name,
                        $rawMessageCopy
                    );
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }

            return Pointer.NULL;
        }
    };

    private static SerializerFunction functionCallback = new SerializerFunction() {
        @SneakyThrows
        @Override
        public void callback(Pointer $functionData, Pointer $resolver, Pointer $rejecter) {
            try {
                Pointer $userData = _SaucerNative.N.saucer_function_data_get_user_data($functionData);
                String rawMessage = $userData.getString(0);
                _SaucerNative.N.saucer_alloc_free($userData);

                System.err.println("POINT CHARLIE");
                System.out.println(rawMessage);
                System.err.println("-------------");
                _SaucerNative.N.saucer_serializer_resolve($resolver, rawMessage);
            } catch (Throwable t) {
                t.printStackTrace();
                String reason = getExceptionStack(t);
                _SaucerNative.N.saucer_serializer_reject($rejecter, reason);
            }
        }
    };

    public static void main(String[] args) throws FileNotFoundException, IOException {
        Pointer $serializer = _SaucerNative.N.saucer_serializer_new();

        // Set up JSON->C serialization.
        _SaucerNative.N.saucer_serializer_set_js_serializer($serializer, "JSON.stringify");

        // Test JS code:
        /*
          for (let i = 0; i < 10000; i++) {
            let res = await saucer.exposed.test("bogus".repeat(100000))
            console.log(res);
          }
         */

        // Handle messages from the webview.
        _SaucerNative.N.saucer_serializer_set_parser($serializer, parserCallback);

        // Handle the incoming calls to us from JS.
        _SaucerNative.N.saucer_serializer_set_function($serializer, functionCallback);

        Pointer $options = _SaucerNative.N.saucer_options_new();
        _SaucerNative.N.saucer_options_set_hardware_acceleration($options, true);

        Pointer $view = _SaucerNative.N.saucer_new($serializer, $options);
        _SaucerNative.N.saucer_window_set_title($view, "My awesome saucer app");
        _SaucerNative.N.saucer_window_set_size($view, 800, 600);
        _SaucerNative.N.saucer_window_show($view);

        _SaucerNative.N.saucer_webview_set_url($view, "https://google.com");
        _SaucerNative.N.saucer_webview_set_dev_tools($view, true);

        _SaucerNative.N.saucer_add_function($view, "test", _SaucerNative.SAUCER_LAUNCH_POLICY_SYNC);

        _SaucerNative.N.saucer_window_run($view);
        System.out.println("Saucer is no longer running!");
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
