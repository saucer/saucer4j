package co.casterlabs.saucer.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.saucer.SaucerColor;
import co.casterlabs.saucer.SaucerFunctionHandler;
import co.casterlabs.saucer.SaucerOptions;
import co.casterlabs.saucer.SaucerView;
import co.casterlabs.saucer.documentation.PointerType;
import lombok.NonNull;
import lombok.SneakyThrows;

@SuppressWarnings("deprecation")
@PointerType
class SaucerViewImpl extends _SaucerPointer implements SaucerView {
    // @formatter:off
    private static final _SmartviewNative N_SMARTVIEW = _SaucerNative.load(_SmartviewNative.class);
    private static final _WebviewNative   N_WEBVIEW   = _SaucerNative.load(_WebviewNative.class);
    private static final _WindowNative    N_WINDOW    = _SaucerNative.load(_WindowNative.class);
    // @formatter:on

    private final _SaucerPointer $options;
    private final @PointerType SaucerSerializer $serializer;

    private Map<String, SaucerFunctionHandler> functions = new HashMap<>();

    public SaucerViewImpl(@NonNull SaucerOptions options) {
        $options = options.toNative();

        $serializer = new SaucerSerializer(new BiFunction<String, JsonArray, JsonElement>() {
            @SneakyThrows
            @Override
            public JsonElement apply(String func, JsonArray args) {
                if (functions.containsKey(func)) {
                    return functions.get(func).handle(args);
                } else {
                    throw new IllegalStateException("Unregistered function");
                }
            }
        });

        this.setup(N_SMARTVIEW.saucer_new(this.$serializer.p(), this.$options.p()), N_SMARTVIEW::saucer_free);

        N_WINDOW.saucer_window_show(this.p());
    }

    @Override
    public boolean isDevtoolsVisible() {
        return N_WEBVIEW.saucer_webview_dev_tools(this.p());
    }

    @Override
    public void setDevtoolsVisible(boolean enabled) {
        N_WEBVIEW.saucer_webview_set_dev_tools(this.p(), enabled);
    }

    @Override
    public String currentUrl() {
        return N_WEBVIEW.saucer_webview_url(this.p());
    }

    @Override
    public void setUrl(@NonNull String url) {
        N_WEBVIEW.saucer_webview_set_url(this.p(), url);
    }

    @Override
    public boolean isContextMenuAllowed() {
        return N_WEBVIEW.saucer_webview_context_menu(this.p());
    }

    @Override
    public void setContextMenuAllowed(boolean enabled) {
        N_WEBVIEW.saucer_webview_set_context_menu(this.p(), enabled);
    }

    @Override
    public void executeJavaScript(@NonNull String scriptToExecute) {
        N_WEBVIEW.saucer_webview_execute(this.p(), scriptToExecute);
    }

    @Override
    public void addFunction(@NonNull String name, @NonNull SaucerFunctionHandler handler) {
        if (this.functions.containsKey(name)) {
            // Just replace the function. Do not tell saucer to re-register.
            this.functions.put(name, handler);
            return;
        } else {
            N_SMARTVIEW.saucer_add_function(this.p(), name, _SmartviewNative.SAUCER_LAUNCH_POLICY_SYNC);
            this.functions.put(name, handler);
        }
    }

    @Override
    public void setBackground(@NonNull SaucerColor color) {
        N_WINDOW.saucer_window_set_background(this.p(), color.p());
    }

    @Override
    public void run() {
        N_WINDOW.saucer_window_run(this.p());
    }

    // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/smartview.h
    static interface _SmartviewNative extends Library {

        static final int SAUCER_LAUNCH_POLICY_SYNC = 0;
//      static final int SAUCER_LAUNCH_POLICY_ASYNC = 1;

        Pointer saucer_new(Pointer $serializer, Pointer $options);

        void saucer_free(Pointer $instance);

        void saucer_add_function(Pointer $instance, String name, int saucerLaunchPolicy);

    }

    // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/webview.h
    static interface _WebviewNative extends Library {

        boolean saucer_webview_dev_tools(Pointer $instance);

        String saucer_webview_url(Pointer $instance);

        boolean saucer_webview_context_menu(Pointer $instance);

        void saucer_webview_set_dev_tools(Pointer $instance, boolean setEnabled);

        void saucer_webview_set_context_menu(Pointer $instance, boolean setEnabled);

        void saucer_webview_set_url(Pointer $instance, String url);

        void saucer_webview_execute(Pointer $instance, String script);

    }

    static interface _WindowNative extends Library {

        void saucer_window_show(Pointer $instance);

        void saucer_window_run(Pointer $instance);

        void saucer_window_set_background(Pointer $instance, Pointer $color);

    }

}
