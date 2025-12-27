package app.saucer.webview.bridge;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;

import com.sun.jna.Callback;

import app.saucer.SaucerApp;
import app.saucer.SaucerDesktop;
import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import app.saucer.ntv.ntv_webview;
import app.saucer.ntv.ntv_webview.saucer_webview;
import app.saucer.ntv.ntv_webview.saucer_webview_event;
import app.saucer.ntv.ntv_webview.saucer_webview_event_message;
import app.saucer.ntv.documentation.InternalUseOnly;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.SaucerResourceUtil;
import app.saucer.ntv.util.size_t;
import app.saucer.webview.SaucerWebview;
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
public final class SaucerBridge {
    private static final String init_fmt = SaucerResourceUtil.loadResourceString("webview/bridge/init_fmt.js");
    private static final String ipc_object_fmt = SaucerResourceUtil.loadResourceString("webview/bridge/ipc_object_fmt.js");

    private final SaucerWebview webview;

    private final saucer_webview_event_message messageCallback = this::onMessage;

    private Map<String, _JavascriptObjectWrapper> objects = new LinkedHashMap<>();

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerBridge(SaucerWebview webview) {
        this.webview = webview;

        ntv_webview.N.saucer_webview_on(SaucerBoxedType.ntv(this.webview), saucer_webview_event.MESSAGE, messageCallback, false, null);

        this.injectScript(
            String.format(
                init_fmt,
                new JsonObject()
                    .put("archTarget", SaucerApp.getArchTarget())
                    .put("systemTarget", SaucerApp.getSystemTarget())
                    .put("backend", SaucerApp.getBackendType().toString())
            ),
            SaucerLoadTime.DOM_CREATION,
            false,
            false
        );

        this.clearAll();
    }

    private boolean onMessage(saucer_webview _unused, String raw, size_t _unused2, Callback _unused3) {
        if (this.webview.isClosed()) {
            return true;
        }

        JsonObject message;
        try {
            message = Rson.DEFAULT.fromJson(raw, JsonObject.class).getObject("message");
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }

        try {
            this.webview.window.dispatchAsync(() -> {
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
                            this.webview.messages.handle(data);
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
                            // TODO
//                        webview.close();
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
                if (requestId == null || this.webview.isClosed()) {
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
        } catch (RejectedExecutionException e) {
            // Executor has been shut down, ignore
        }

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
        ntv_webview.N.saucer_webview_execute(SaucerBoxedType.ntv(this.webview), scriptToExecute);
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
            String.format(
                "{\n" + ipc_object_fmt + "\n}",
                new JsonString(wrapper.id),
                new JsonString(wrapper.path),
                Rson.DEFAULT.toJson(wrapper.functions()),
                Rson.DEFAULT.toJson(wrapper.properties())
            ),
            SaucerLoadTime.DOM_CREATION,
            false,
            false
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

    /**
     * @return a script id that can be used to remove the script later.
     */
    public long injectScript(@NonNull String code, SaucerLoadTime runAt, boolean disallowFrames, boolean clearable) {
        size_t id = ntv_webview.N.saucer_webview_inject(SaucerBoxedType.ntv(this.webview), code, runAt.nativeValue, disallowFrames, clearable);
        return id.longValue();
    }

    public void clear(long scriptId) {
        ntv_webview.N.saucer_webview_uninject(SaucerBoxedType.ntv(this.webview), new size_t(scriptId));
    }

    /**
     * Clears all injected scripts and removes defined objects.
     * 
     * Note that permanent scripts will never be removed by this.
     */
    public void clearAll() {
        ntv_webview.N.saucer_webview_uninject_all(SaucerBoxedType.ntv(this.webview));
        this.objects.clear();
        this.defineObject("saucer.webview", this.webview);
        this.defineObject("saucer.window", this.webview.window);
    }

}
