package co.casterlabs.saucer._impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import co.casterlabs.saucer.SaucerBridge;
import co.casterlabs.saucer._impl.ImplSaucerBridge._Native.MessageCallback;
import co.casterlabs.saucer.bridge.JavascriptObject;
import lombok.NonNull;
import lombok.SneakyThrows;

@SuppressWarnings("deprecation")
class ImplSaucerBridge implements SaucerBridge {
    private static final _Native N = _SaucerNative.load(_Native.class);

    private static final String init = Resources.loadResourceString("bridge/init.js");
    private static final String ipc_object_fmt = Resources.loadResourceString("bridge/ipc_object_fmt.js");

    private _ImplSaucer saucer;

    private Map<String, JavascriptObjectWrapper> objects = new HashMap<>();

    private MessageCallback messageCallback = (Pointer $raw) -> {
        JsonObject message;
        try {
            String raw = $raw.getString(0, "UTF-8");
            message = Rson.DEFAULT.fromJson(raw, JsonObject.class);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }

        JsonElement returnValue = null;
        boolean isError = false;
        try {
            switch (message.getString("type")) {
                case "GET": {
                    JavascriptObjectWrapper object = this.objects.get(message.getString("objectId"));
                    assert object != null : "Unknown objectId: " + message;

                    // RPC.get("objectId", "propertyName");
                    returnValue = object.handleGet(message.getString("propertyName"));
                    break;
                }

                case "SET": {
                    JavascriptObjectWrapper object = this.objects.get(message.getString("objectId"));
                    assert object != null : "Unknown objectId: " + message;

                    // RPC.set("objectId", "propertyName", newValue);
                    object.handleSet(message.getString("propertyName"), message.get("newValue"));
                    break;
                }

                case "INVOKE": {
                    JavascriptObjectWrapper object = this.objects.get(message.getString("objectId"));
                    assert object != null : "Unknown objectId: " + message;

                    // RPC.invoke("objectId", "functionName", Array.from(arguments));
                    returnValue = object.handleInvoke(message.getString("functionName"), message.getArray("arguments"));
                    break;
                }

                case "MESSAGE": {
                    JsonElement data = message.get("data");
                    saucer.messages.handle(data);
                    break;
                }

                case "CHECK_MUTATION": {
                    JsonObject newValues = new JsonObject();
                    for (JavascriptObjectWrapper object : this.objects.values()) {
                        for (String name : object.whichFieldsHaveMutated()) {
                            newValues.put(object.id + '|' + name, object.handleGet(name));
                        }
                    }
                    returnValue = newValues;
                    break;
                }

                case "CLOSE": {
                    saucer.close();
                    break;
                }

                default:
                    throw new IllegalArgumentException("Unrecognized call: " + message);
            }
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            t.printStackTrace(pw);

            String out = sw.toString();

            pw.flush();
            pw.close();
            sw.flush();

            String full = out
                .substring(0, out.length() - 2)
                .replace("\r", "");

            System.err.printf("An error occurred whilst processing function, bubbling to JavaScript.\n%s\n", full);
            returnValue = new JsonString(full);
            isError = true;
        }

        if (!message.containsKey("requestId")) {
            return true; // Drop the response.
        }

        JsonElement requestId = message.get("requestId");
        if (isError) {
            saucer.webview.executeJavaScript(
                String.format(
                    "if (window.saucer.__rpc.waiting[%s]) window.saucer.__rpc.waiting[%s].reject(%s);",
                    requestId, requestId, returnValue
                )
            );
        } else {
            saucer.webview.executeJavaScript(
                String.format(
                    "if (window.saucer.__rpc.waiting[%s]) window.saucer.__rpc.waiting[%s].resolve(%s);",
                    requestId, requestId, returnValue
                )
            );
        }

        return true;
    };

    ImplSaucerBridge(_ImplSaucer saucer) {
        this.saucer = saucer;

        N.saucer_webview_on_message(this.saucer, this.messageCallback);

        this.defineObject("saucer.webview", this.saucer.webview());
        this.defineObject("saucer.window", this.saucer.window());
    }

    @SneakyThrows
    @Override
    public synchronized void defineObject(@NonNull String name, @NonNull Object obj) {
        assert obj.getClass().isAnnotationPresent(JavascriptObject.class) : "Class MUST be annotated with @JavascriptObject";

        JavascriptObjectWrapper wrapper = new JavascriptObjectWrapper(name, obj);
        this.objects.put(wrapper.id, wrapper);

        // Look for sub-objects and register them.
        // Note that this recurses until there are no more sub-objects.
        for (Field f : obj.getClass().getFields()) {
            if (f.getType().isAnnotationPresent(JavascriptObject.class)) {
                this.defineObject(name + "." + f.getName(), f.get(obj));
            }
        }
    }

    @Override
    public synchronized void apply() {
        List<String> lines = new LinkedList<>();
        lines.add(init);

        for (JavascriptObjectWrapper object : this.objects.values()) {
            lines.add(
                String.format(
                    "{\n" + ipc_object_fmt + "\n}",
                    new JsonString(object.id),
                    new JsonString(object.path),
                    Rson.DEFAULT.toJson(object.functions()),
                    Rson.DEFAULT.toJson(object.properties())
                )
            );
        }

        String finalScript = "if (window.self === window.top) {\n" + String.join("\n\n", lines) + "\n}";

        N.saucer_webview_clear_scripts(this.saucer);
        N.saucer_webview_inject(this.saucer, finalScript, _Native.SAUCER_LOAD_TIME_CREATION, _Native.SAUCER_WEB_FRAME_TOP);
        saucer.webview().reload();
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    // https://github.com/saucer/bindings/blob/main/include/saucer/webview.h
    static interface _Native extends Library {
        static final int SAUCER_LOAD_TIME_CREATION = 0;
//        static final int SAUCER_LOAD_TIME_READY = 1;

        static final int SAUCER_WEB_FRAME_TOP = 0;

        void saucer_webview_clear_scripts(_ImplSaucer saucer);

        void saucer_webview_inject(_ImplSaucer saucer, String javascript, int loadTime, int framePolicy);

        void saucer_webview_on_message(_ImplSaucer saucer, MessageCallback callback);

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface MessageCallback extends Callback {
            boolean callback(Pointer $message);
        }

    }

}
