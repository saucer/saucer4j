package app.saucer.webview.bridge;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import app.saucer.Saucer;
import app.saucer.SaucerApp;
import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import app.saucer.ntv._script.saucer_script;
import app.saucer.ntv._webview;
import app.saucer.ntv._webview.saucer_on_message;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.SaucerResourceUtil;
import app.saucer.util.desktop.SaucerDesktop;
import app.saucer.webview.SaucerScript;
import app.saucer.webview.SaucerScript.SaucerFramePolicy;
import app.saucer.webview.SaucerScript.SaucerLoadTime;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * @apiNote This class is not thread-safe. You must call it from the main thread
 *          or use {@link SaucerApp#dispatch(Runnable)} or
 *          {@link SaucerApp#dispatch(Supplier)}.
 */
@SuppressWarnings("deprecation")
public class SaucerBridge {
    private static final String init_fmt = SaucerResourceUtil.loadResourceString("webview/bridge/init_fmt.js");
    private static final String ipc_object_fmt = SaucerResourceUtil.loadResourceString("webview/bridge/ipc_object_fmt.js");

    private final Saucer saucer;
    private final ExecutorService asyncExecutor;

    private final saucer_on_message messageCallback = this::onMessage;

    private Map<String, _JavascriptObjectWrapper> objects = new LinkedHashMap<>();

    /**
     * @deprecated Native interop only.
     * 
     * @implNote   This class does not free() itself automatically, which differs
     *             from most BoxedTypes.
     */
    @Deprecated
    public SaucerBridge(Saucer saucer, ExecutorService asyncExecutor) {
        this.saucer = saucer;
        this.asyncExecutor = asyncExecutor;

        _webview.N.saucer_webview_on_message(this.saucer.ntv(), this.messageCallback);

        this.injectScript(
            SaucerScript.create(
                String.format(
                    init_fmt,
                    new JsonObject()
                        .put("archTarget", Saucer.getArchTarget())
                        .put("systemTarget", Saucer.getSystemTarget())
                        .put("backend", Saucer.getBackendType().toString())
                ),
                SaucerLoadTime.DOM_CREATION
            )
                .permanent(true)
                .framePolicy(SaucerFramePolicy.TOP)
        );

        this.clear();
    }

    private boolean onMessage(String raw) {
        if (this.saucer.isClosed()) {
            return true;
        }

        JsonObject message;
        try {
            message = Rson.DEFAULT.fromJson(raw, JsonObject.class);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }

        this.asyncExecutor.execute(() -> {
            JsonElement returnValue = null;
            boolean isError = false;
            try {
                switch (message.getString("type")) {
                    case "GET": {
                        _JavascriptObjectWrapper object = this.objects.get(message.getString("objectId"));
                        assert object != null : "Unknown objectId: " + message;

                        // RPC.get("objectId", "propertyName");
                        returnValue = object.handleGet(message.getString("propertyName"));
                        break;
                    }

                    case "SET": {
                        _JavascriptObjectWrapper object = this.objects.get(message.getString("objectId"));
                        assert object != null : "Unknown objectId: " + message;

                        // RPC.set("objectId", "propertyName", newValue);
                        object.handleSet(message.getString("propertyName"), message.get("newValue"));
                        break;
                    }

                    case "INVOKE": {
                        _JavascriptObjectWrapper object = this.objects.get(message.getString("objectId"));
                        assert object != null : "Unknown objectId: " + message;

                        // RPC.invoke("objectId", "functionName", Array.from(arguments));
                        returnValue = object.handleInvoke(message.getString("functionName"), message.getArray("arguments"));
                        break;
                    }

                    case "MESSAGE": {
                        JsonElement data = message.get("data");
                        this.saucer.messages().handle(data);
                        break;
                    }

                    case "CHECK_MUTATION": {
                        JsonObject newValues = new JsonObject();
                        for (_JavascriptObjectWrapper object : this.objects.values()) {
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

            JsonElement requestId = message.get("requestId");
            if (requestId == null || this.saucer.isClosed()) {
                // Drop the response.
            } else if (isError) {
                String js = String.format(
                    "if (window.saucer.__rpc.waiting[%s]) window.saucer.__rpc.waiting[%s].reject(%s);",
                    requestId, requestId, returnValue
                );
                SaucerApp.dispatch(
                    () -> this.executeJavaScript(js)
                );
            } else {
                String js = String.format(
                    "if (window.saucer.__rpc.waiting[%s]) window.saucer.__rpc.waiting[%s].resolve(%s);",
                    requestId, requestId, returnValue
                );
                SaucerApp.dispatch(
                    () -> this.executeJavaScript(js)
                );
            }
        });

        return true;
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * Executes the given JavaScript code in the webview.
     */
    @JavascriptFunction
    public void executeJavaScript(@NonNull String scriptToExecute) {
        _webview.N.saucer_webview_execute(this.saucer.ntv(), scriptToExecute);
    }

    /**
     * @param obj your class annotated with {@link JavascriptObject}.
     */
    @SneakyThrows
    public void defineObject(@NonNull String name, @NonNull Object obj) {
        assert obj.getClass().isAnnotationPresent(JavascriptObject.class) : "Class MUST be annotated with @JavascriptObject";

        _JavascriptObjectWrapper wrapper = new _JavascriptObjectWrapper(name, obj);
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
                SaucerLoadTime.DOM_CREATION
            )
                .permanent(true)
                .framePolicy(SaucerFramePolicy.TOP)
        );

        // Look for sub-objects and register them.
        // Note that this recurses until there are no more sub-objects.
        for (Field f : _Reflection.getAllFields(obj.getClass())) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            if (f.getType().isAnnotationPresent(JavascriptObject.class)) {
                f.setAccessible(true);
                this.defineObject(name + "." + f.getName(), f.get(obj));
            }
        }
    }

    public void injectScript(@NonNull SaucerScript script) {
        saucer_script scriptNtv = SaucerBoxedType.ntv(script);
        _webview.N.saucer_webview_inject(this.saucer.ntv(), scriptNtv);
    }

    /**
     * Clears all injected scripts and removes defined objects.
     * 
     * Note that permanent scripts will never be removed by this.
     */
    public void clear() {
        _webview.N.saucer_webview_clear_scripts(this.saucer.ntv());
        this.objects.clear();
        this.defineObject("saucer.webview", this.saucer.webview());
        this.defineObject("saucer.window", this.saucer.window());
    }

}
