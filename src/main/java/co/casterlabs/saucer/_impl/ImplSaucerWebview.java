package co.casterlabs.saucer._impl;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.SaucerWebview;
import co.casterlabs.saucer._impl.ImplSaucerWebview._Native.WebviewSchemeCallback;
import co.casterlabs.saucer._impl.ImplSaucerWebview._Native.WebviewURLChangedEventCallback;
import co.casterlabs.saucer._impl.ImplSaucerWebview._Native.WebviewVoidCallback;
import co.casterlabs.saucer._impl.ImplSaucerWindow._Native.WindowVoidCallback;
import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.bridge.JavascriptGetter;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.bridge.JavascriptSetter;
import co.casterlabs.saucer.documentation.PointerType;
import co.casterlabs.saucer.scheme.SaucerSchemeHandler;
import co.casterlabs.saucer.scheme.SaucerSchemeRequest;
import co.casterlabs.saucer.scheme.SaucerSchemeResponse;
import co.casterlabs.saucer.scheme.SaucerSchemeResponse.SaucerRequestError;
import lombok.NonNull;

@SuppressWarnings("deprecation")
@JavascriptObject
class ImplSaucerWebview implements SaucerWebview {
    private static final _Native N = _SaucerNative.load(_Native.class);
    private static final String CUSTOM_SCHEME = "saucer";

    static {
        N.saucer_register_scheme(CUSTOM_SCHEME);
    }

    private final @PointerType ImplSaucer $saucer;

    private @Nullable SaucerWebviewListener eventListener;
    private @Nullable SaucerSchemeHandler schemeHandler;

    private WebviewSchemeCallback schemeHandlerCallback = (Pointer _unused, Pointer $request) -> {
        SaucerSchemeRequest request = new SaucerSchemeRequest($request);

        SaucerSchemeResponse response;
        if (this.schemeHandler == null) {
            response = new SaucerSchemeResponse(SaucerRequestError.SAUCER_REQUEST_ERROR_NOT_FOUND);
        } else {
            try {
                response = this.schemeHandler.handle(request);
            } catch (Throwable t) {
                t.printStackTrace();
                response = new SaucerSchemeResponse(SaucerRequestError.SAUCER_REQUEST_ERROR_FAILED);
            }
        }

        response.freeIsExternalNow(); // Bork the safety. We're in saucer's hands now.
        return response.p();
    };

    private WebviewVoidCallback webEventLoadFinishedCallback = (Pointer $saucer) -> {
        try {
//            System.out.println("webLoadFinished");
            if (this.eventListener == null) return;
            this.eventListener.onLoadFinished();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WebviewVoidCallback webEventLoadStartedCallback = (Pointer $saucer) -> {
        try {
//            System.out.println("loadStarted");
            if (this.eventListener == null) return;
            this.eventListener.onLoadStarted();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WebviewURLChangedEventCallback webEventUrlChangedCallback = (Pointer $saucer, String newUrl) -> {
        try {
//            System.out.printf("urlChanged: %s\n", newUrl);
            if (this.eventListener == null) return;
            this.eventListener.onUrlChanged(newUrl);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WebviewVoidCallback webEventDomReadyCallback = (Pointer $saucer) -> {
        try {
//            System.out.println("domReady");
            if (this.eventListener == null) return;
            this.eventListener.onDomReady();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    ImplSaucerWebview(ImplSaucer $saucer) {
        this.$saucer = $saucer;

        N.saucer_webview_on($saucer.p(), _Native.SAUCER_WEB_EVENT_LOAD_FINISHED, this.webEventLoadFinishedCallback);
        N.saucer_webview_on($saucer.p(), _Native.SAUCER_WEB_EVENT_LOAD_STARTED, this.webEventLoadStartedCallback);
        N.saucer_webview_on($saucer.p(), _Native.SAUCER_WEB_EVENT_URL_CHANGED, this.webEventUrlChangedCallback);
        N.saucer_webview_on($saucer.p(), _Native.SAUCER_WEB_EVENT_DOM_READY, this.webEventDomReadyCallback);

        N.saucer_webview_handle_scheme($saucer.p(), CUSTOM_SCHEME, this.schemeHandlerCallback);
    }

    @JavascriptGetter("devtoolsVisible")
    @Override
    public boolean isDevtoolsVisible() {
        return N.saucer_webview_dev_tools($saucer.p());
    }

    @JavascriptSetter("devtoolsVisible")
    @Override
    public void setDevtoolsVisible(boolean show) {
        N.saucer_webview_set_dev_tools($saucer.p(), show);
    }

    @JavascriptGetter("currentUrl")
    @Override
    public String currentUrl() {
        return N.saucer_webview_url($saucer.p());
    }

    @JavascriptSetter("currentUrl")
    @Override
    public void setUrl(@NonNull String url) {
        N.saucer_webview_set_url($saucer.p(), url);
    }

    @JavascriptFunction
    @Override
    public void serveScheme(@NonNull String path) {
        N.saucer_webview_serve_scheme($saucer.p(), path, CUSTOM_SCHEME);
    }

    @JavascriptGetter("contextMenuAllowed")
    @Override
    public boolean isContextMenuAllowed() {
        return N.saucer_webview_context_menu($saucer.p());
    }

    @JavascriptSetter("contextMenuAllowed")
    @Override
    public void setContextMenuAllowed(boolean allowed) {
        N.saucer_webview_set_context_menu($saucer.p(), allowed);
    }

    @JavascriptFunction
    @Override
    public void executeJavaScript(@NonNull String scriptToExecute) {
        N.saucer_webview_execute($saucer.p(), '{' + scriptToExecute + '}');
    }

    @Override
    public void setListener(@Nullable SaucerWebviewListener listener) {
        this.eventListener = listener;
    }

    @Override
    public void setSchemeHandler(@Nullable SaucerSchemeHandler handler) {
        this.schemeHandler = handler;
    }

    // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/webview.h
    static interface _Native extends Library {
        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WEB_EVENT_LOAD_FINISHED = 0;

        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WEB_EVENT_LOAD_STARTED = 1;

        /** Requires {@link URLChangedEventCallback} */
        static final int SAUCER_WEB_EVENT_URL_CHANGED = 2;

        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WEB_EVENT_DOM_READY = 3;

        boolean saucer_webview_dev_tools(Pointer $saucer);

        String saucer_webview_url(Pointer $saucer);

        boolean saucer_webview_context_menu(Pointer $saucer);

        void saucer_webview_set_dev_tools(Pointer $saucer, boolean setEnabled);

        void saucer_webview_set_context_menu(Pointer $saucer, boolean setEnabled);

        void saucer_webview_set_url(Pointer $saucer, String url);

        void saucer_webview_handle_scheme(Pointer $saucer, String scheme, WebviewSchemeCallback callback);

        void saucer_webview_serve_scheme(Pointer $saucer, String path, String scheme);

        void saucer_webview_execute(Pointer $saucer, String script);

        long saucer_webview_on(Pointer $saucer, int saucerWebEvent, Callback callback);

        void saucer_register_scheme(String name);

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface WebviewURLChangedEventCallback extends Callback {
            void callback(Pointer $saucer, String newUrl);
        }

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface WebviewVoidCallback extends Callback {
            void callback(Pointer $saucer);
        }

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface WebviewSchemeCallback extends Callback {
            Pointer callback(Pointer $saucer, Pointer $request);
        }

    }

}
