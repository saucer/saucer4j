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
import co.casterlabs.saucer.documentation.NoFree;
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
        N.saucer_register_scheme(_SafePointer.allocate(CUSTOM_SCHEME));
    }

    private final ImplSaucer saucer;

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

    private WebviewVoidCallback webEventLoadFinishedCallback = (Pointer _unused) -> {
        try {
//            System.out.println("webLoadFinished");
            if (this.eventListener == null) return;
            this.eventListener.onLoadFinished();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WebviewVoidCallback webEventLoadStartedCallback = (Pointer _unused) -> {
        try {
//            System.out.println("loadStarted");
            if (this.eventListener == null) return;
            this.eventListener.onLoadStarted();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WebviewURLChangedEventCallback webEventUrlChangedCallback = (Pointer _unused, _SafePointer $newUrl) -> {
        try {
            if (this.eventListener == null) return;
//            System.out.printf("urlChanged: %s\n", newUrl);
            String newUrl = $newUrl.p().getString(0);
            this.eventListener.onUrlChanged(newUrl);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WebviewVoidCallback webEventDomReadyCallback = (Pointer _unused) -> {
        try {
//            System.out.println("domReady");
            if (this.eventListener == null) return;
            this.eventListener.onDomReady();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    ImplSaucerWebview(ImplSaucer saucer) {
        this.saucer = saucer;

        N.saucer_webview_on(this.saucer.$handle, _Native.SAUCER_WEB_EVENT_LOAD_FINISHED, this.webEventLoadFinishedCallback);
        N.saucer_webview_on(this.saucer.$handle, _Native.SAUCER_WEB_EVENT_LOAD_STARTED, this.webEventLoadStartedCallback);
        N.saucer_webview_on(this.saucer.$handle, _Native.SAUCER_WEB_EVENT_URL_CHANGED, this.webEventUrlChangedCallback);
        N.saucer_webview_on(this.saucer.$handle, _Native.SAUCER_WEB_EVENT_DOM_READY, this.webEventDomReadyCallback);

        N.saucer_webview_handle_scheme(this.saucer.$handle, _SafePointer.allocate(CUSTOM_SCHEME), this.schemeHandlerCallback);
    }

    @JavascriptGetter("devtoolsVisible")
    @Override
    public boolean isDevtoolsVisible() {
        return N.saucer_webview_dev_tools(this.saucer.$handle);
    }

    @JavascriptSetter("devtoolsVisible")
    @Override
    public void setDevtoolsVisible(boolean show) {
        N.saucer_webview_set_dev_tools(this.saucer.$handle, show);
    }

    @JavascriptGetter("currentUrl")
    @Override
    public String currentUrl() {
        _SafePointer $url = N.saucer_webview_url(this.saucer.$handle);
        return $url.p().getString(0);
    }

    @JavascriptSetter("currentUrl")
    @Override
    public void setUrl(@NonNull String url) {
        N.saucer_webview_set_url(this.saucer.$handle, _SafePointer.allocate(url));
    }

    @JavascriptFunction
    @Override
    public void serveScheme(@NonNull String path) {
        N.saucer_webview_serve_scheme(this.saucer.$handle, _SafePointer.allocate(path), _SafePointer.allocate(CUSTOM_SCHEME));
    }

    @JavascriptGetter("contextMenuAllowed")
    @Override
    public boolean isContextMenuAllowed() {
        return N.saucer_webview_context_menu(this.saucer.$handle);
    }

    @JavascriptSetter("contextMenuAllowed")
    @Override
    public void setContextMenuAllowed(boolean allowed) {
        N.saucer_webview_set_context_menu(this.saucer.$handle, allowed);
    }

    @JavascriptFunction
    @Override
    public void executeJavaScript(@NonNull String scriptToExecute) {
        scriptToExecute = '{' + scriptToExecute + '}';

        N.saucer_webview_execute(this.saucer.$handle, _SafePointer.allocate(scriptToExecute));
    }

    @Override
    public void setListener(@Nullable SaucerWebviewListener listener) {
        this.eventListener = listener;
    }

    @Override
    public void setSchemeHandler(@Nullable SaucerSchemeHandler handler) {
        this.schemeHandler = handler;
    }

    // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/webview.h
    static interface _Native extends Library {
        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WEB_EVENT_LOAD_FINISHED = 0;

        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WEB_EVENT_LOAD_STARTED = 1;

        /** Requires {@link URLChangedEventCallback} */
        static final int SAUCER_WEB_EVENT_URL_CHANGED = 2;

        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WEB_EVENT_DOM_READY = 3;

        boolean saucer_webview_dev_tools(_SafePointer $saucer);

        _SafePointer saucer_webview_url(_SafePointer $saucer);

        boolean saucer_webview_context_menu(_SafePointer $saucer);

        void saucer_webview_set_dev_tools(_SafePointer $saucer, boolean setEnabled);

        void saucer_webview_set_context_menu(_SafePointer $saucer, boolean setEnabled);

        void saucer_webview_set_url(_SafePointer $saucer, _SafePointer $url);

        void saucer_webview_handle_scheme(_SafePointer $saucer, _SafePointer $scheme, WebviewSchemeCallback callback);

        void saucer_webview_serve_scheme(_SafePointer $saucer, _SafePointer $path, _SafePointer $scheme);

        void saucer_webview_execute(_SafePointer $saucer, _SafePointer $script);

        long saucer_webview_on(_SafePointer $saucer, int saucerWebEvent, Callback callback);

        void saucer_register_scheme(_SafePointer $name);

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface WebviewURLChangedEventCallback extends Callback {
            void callback(Pointer $saucer, _SafePointer $newUrl);
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
            Pointer callback(Pointer $saucer, @NoFree Pointer $request);
        }

    }

}
