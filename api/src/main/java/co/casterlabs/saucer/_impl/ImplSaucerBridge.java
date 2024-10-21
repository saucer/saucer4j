package co.casterlabs.saucer._impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.SaucerBridge;
import co.casterlabs.saucer._impl.ImplSaucerBridge._Native.MessageCallback;
import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.utils.SaucerDesktop;
import co.casterlabs.saucer.utils.SaucerScript;
import co.casterlabs.saucer.utils.SaucerScript.SaucerFramePolicy;
import co.casterlabs.saucer.utils.SaucerScript.SaucerLoadTime;
import lombok.NonNull;
import lombok.SneakyThrows;

@SuppressWarnings("deprecation")
class ImplSaucerBridge implements SaucerBridge {
    private static final _Native N = _SaucerNative.load(_Native.class);

    private static final String init_fmt = Resources.loadResourceString("bridge/init_fmt.js");
    private static final String ipc_object_fmt = Resources.loadResourceString("bridge/ipc_object_fmt.js");

    private _ImplSaucer saucer;

    private Map<String, JavascriptObjectWrapper> objects = new LinkedHashMap<>();

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

                case "OPEN_LINK": {
                    String link = message.getString("link");
                    SaucerDesktop.open(link);
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
            this.executeJavaScript(
                String.format(
                    "if (window.saucer.__rpc.waiting[%s]) window.saucer.__rpc.waiting[%s].reject(%s);",
                    requestId, requestId, returnValue
                )
            );
        } else {
            this.executeJavaScript(
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

        this.injectScript(
            SaucerScript.createPermanent(
                String.format(
                    init_fmt,
                    new JsonObject()
                        .put("archTarget", Saucer.getArchTarget())
                        .put("systemTarget", Saucer.getSystemTarget())
                        .put("backend", Saucer.getBackend())
                ),
                SaucerLoadTime.DOM_CREATION,
                SaucerFramePolicy.TOP
            )
        );

        this.clear();
    }

    @JavascriptFunction
    @Override
    public void executeJavaScript(@NonNull String scriptToExecute) {
        N.saucer_webview_execute(this.saucer, '{' + scriptToExecute + '}');
    }

    @SneakyThrows
    @Override
    public synchronized void defineObject(@NonNull String name, @NonNull Object obj) {
        assert obj.getClass().isAnnotationPresent(JavascriptObject.class) : "Class MUST be annotated with @JavascriptObject";

        JavascriptObjectWrapper wrapper = new JavascriptObjectWrapper(name, obj);
        this.objects.put(wrapper.id, wrapper);

        this.injectScript(
            SaucerScript.create(
                String.format(
                    "{\n" + ipc_object_fmt + "\n}",
                    new JsonString(wrapper.id),
                    new JsonString(wrapper.path),
                    Rson.DEFAULT.toJson(wrapper.functions()),
                    Rson.DEFAULT.toJson(wrapper.properties())
                ),
                SaucerLoadTime.DOM_CREATION,
                SaucerFramePolicy.TOP
            )
        );

        // Look for sub-objects and register them.
        // Note that this recurses until there are no more sub-objects.
        for (Field f : JavascriptObjectWrapper.getAllFields(obj, obj.getClass())) {
            f.setAccessible(true);

            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            if (f.getType().isAnnotationPresent(JavascriptObject.class)) {
                this.defineObject(name + "." + f.getName(), f.get(obj));
            }
        }
    }

    @Override
    public void injectScript(@NonNull SaucerScript script) {
        N.saucer_webview_inject(this.saucer, script);
    }

    @Override
    public synchronized void clear() {
        N.saucer_webview_clear_scripts(this.saucer);
        this.objects.clear();
        this.defineObject("saucer.webview", this.saucer.webview());
        this.defineObject("saucer.window", this.saucer.window());
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    // https://github.com/saucer/bindings/blob/main/include/saucer/webview.h
    static interface _Native extends Library {

        void saucer_webview_on_message(_ImplSaucer saucer, MessageCallback callback);

        void saucer_webview_execute(_ImplSaucer saucer, String script);

        void saucer_webview_clear_scripts(_ImplSaucer saucer);

        void saucer_webview_inject(_ImplSaucer saucer, SaucerScript script);

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface MessageCallback extends Callback {
            boolean callback(Pointer $message);
        }

    }

}
