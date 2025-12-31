package app.saucer.webview.bridge;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
import app.saucer.ntv.backends.SaucerBackendType;
import app.saucer.ntv.documentation.InternalUseOnly;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.SaucerResourceUtil;
import app.saucer.ntv.util.size_t;
import app.saucer.webview.SaucerWebview;
import app.saucer.webview.window.SaucerWindowDecoration;
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

    static final boolean GENERATE_TYPESCRIPT_DEFINITIONS = Boolean.getBoolean("saucer.generate_typescript_definitions");

    private final SaucerWebview webview;
    private final saucer_webview_event_message messageCallback = this::onMessage;

    private final Map<String, _JavascriptObjectWrapper> objects = new LinkedHashMap<>();
    private final List<_ObjectDescription> objectDescriptions = new ArrayList<>();

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
                    .put("archTarget", SaucerApp.archTarget())
                    .put("systemTarget", SaucerApp.systemTarget())
                    .put("backend", SaucerApp.backendType().toString())
            ),
            SaucerLoadTime.DOM_CREATION,
            true,
            false
        );

        this.injectBase();
    }

    private void injectBase() {
        this.defineObject("saucer.webview", this.webview);
        this.defineObject("saucer.window", this.webview.window);
        this.defineObject("saucer.app", SaucerApp.class);
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

                        case "OPEN_LINK": {
                            String link = message.getString("link");
                            SaucerDesktop.open(link);
                            break;
                        }

                        case "GENERATE_TS_DEFINITIONS": {
                            String definitions = this.generateTypeScriptDefinitions();
                            returnValue = new JsonString(definitions);
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
     * 
     * @return this instance, for chaining.
     */
    @JavascriptFunction
    public SaucerBridge executeJavaScript(@NonNull String scriptToExecute) {
        ntv_webview.N.saucer_webview_execute(SaucerBoxedType.ntv(this.webview), scriptToExecute);
        return this;
    }

    /**
     * @param  obj your class annotated with {@link JavascriptObject}. Can also be
     *             an instance of a class (i.e an object).
     * 
     * @return     this instance, for chaining.
     */
    public SaucerBridge defineObject(@NonNull String name, @NonNull Object obj) {
        _JavascriptObjectWrapper wrapper = this.internal_defineObject(name, obj);
        if (GENERATE_TYPESCRIPT_DEFINITIONS) {
            this.objectDescriptions.add(wrapper.description);
        }
        return this;
    }

    @SneakyThrows
    private _JavascriptObjectWrapper internal_defineObject(String name, Object obj) {
        Class<?> clazz;
        if (obj instanceof Class<?>) {
            clazz = (Class<?>) obj;
            obj = null; // Static class
        } else {
            clazz = obj.getClass();
        }

        assert clazz.isAnnotationPresent(JavascriptObject.class) : "Class MUST be annotated with @JavascriptObject";

        _JavascriptObjectWrapper wrapper = new _JavascriptObjectWrapper(name, clazz, obj);
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
            true,
            true
        );

        // Look for sub-objects and register them.
        // Note that this recurses until there are no more sub-objects.
        for (Field f : _Reflection.getAllFields(clazz)) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            if (!f.getType().isAnnotationPresent(JavascriptObject.class)) continue;

            f.setAccessible(true);

            _JavascriptObjectWrapper sub = this.internal_defineObject(name + "." + f.getName(), f.get(obj));
            if (GENERATE_TYPESCRIPT_DEFINITIONS) {
                wrapper.description.subObjects.add(sub.description);
            }
        }

        return wrapper;
    }

    /**
     * @param  code           the JavaScript code to inject.
     * @param  runAt          when to run the script.
     * @param  disallowFrames whether to disallow the script from running in
     *                        iframes.
     * @param  clearable      whether the script can be removed when
     *                        {@link #clearAll()} or {@link #clear(long)} is called.
     * 
     * @return                a script id that can be used to remove the script
     *                        later.
     */
    public long injectScript(@NonNull String code, SaucerLoadTime runAt, boolean disallowFrames, boolean clearable) {
        size_t id = ntv_webview.N.saucer_webview_inject(SaucerBoxedType.ntv(this.webview), code, runAt.nativeValue, disallowFrames, clearable);
        return id.longValue();
    }

    /**
     * Removes an injected script.
     * 
     * @return this instance, for chaining.
     */
    public SaucerBridge clear(long scriptId) {
        ntv_webview.N.saucer_webview_uninject(SaucerBoxedType.ntv(this.webview), new size_t(scriptId));
        return this;
    }

    /**
     * Clears all injected scripts and removes defined objects.
     * 
     * Note that non-clearable scripts will never be removed by this.
     * 
     * @return this instance, for chaining.
     */
    public SaucerBridge clearAll() {
        ntv_webview.N.saucer_webview_uninject_all(SaucerBoxedType.ntv(this.webview));
        this.objects.clear();
        this.injectBase();
        return this;
    }

    private String generateTypeScriptDefinitions() {
        if (!GENERATE_TYPESCRIPT_DEFINITIONS) {
            throw new IllegalStateException("TypeScript definition generation is disabled. (Set -Dsaucer.generate_typescript_definitions=true to enable it)");
        }

        List<String> lines = new ArrayList<>();

        lines.add("// Auto-generated Saucer Bridge Definitions");
        lines.add(String.format("// Generated on %s", Instant.now().toString()));
        lines.add("");

        lines.add("export declare type MutationListenerId = any;");
        lines.add("declare interface MutationObject<M> {");
        lines.add("    onMutate(propertyName: M, handler: (newValue: any) => void): MutationListenerId;");
        lines.add("    offMutate(id: MutationListenerId): void;");
        lines.add("}");

        // NB: Keep these in sync with their Java counterparts!

        lines.add("export declare type SaucerUrl = string;");
        lines.add(String.format("export declare type SaucerWindowDecoration = '%s';", String.join("' | '", names(SaucerWindowDecoration.values()))));
        lines.add(String.format("export declare type SaucerBackendType = '%s';", String.join("' | '", names(SaucerBackendType.values()))));

        lines.add("export declare interface SaucerColor { r: number; g: number; b: number; a: number; }");
        lines.add("export declare interface SaucerSize { width: number; height: number; }");
        lines.add("export declare interface SaucerPosition { x: number; y: number; }");
        lines.add("export declare interface SaucerRectangle { x: number; y: number; width: number; height: number; }");
        lines.add("export declare interface SaucerScreen { name: string; size: SaucerSize; position: SaucerPosition; }");

        lines.add("");

        for (_ObjectDescription objDesc : this.objectDescriptions) {
            lines.add(objDesc.generateTypescriptDefinition());
        }

        lines.add("");

        lines.add("declare global {");
        lines.add("    interface Window {");

        final List<String> SPECIAL_KEYS = List.of("saucer.window", "saucer.webview", "saucer.app");

        for (_ObjectDescription objDesc : this.objectDescriptions) {
            if (SPECIAL_KEYS.contains(objDesc.path)) continue;
            lines.add(String.format("        readonly %s: %s;", objDesc.path, objDesc.path));
        }

        lines.add("        saucer: { window: saucer_window, webview: saucer_webview, app: saucer_app}");

        lines.add("    }");
        lines.add("}");

        return String.join("\n", lines);
    }

    private static List<String> names(Enum<?>[] enums) {
        List<String> names = new ArrayList<>();
        for (Enum<?> e : enums) {
            names.add(e.name());
        }
        return names;
    }

}
