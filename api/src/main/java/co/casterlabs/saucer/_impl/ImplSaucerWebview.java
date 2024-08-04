package co.casterlabs.saucer._impl;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.SaucerWebview;
import co.casterlabs.saucer._impl.ImplSaucerWebview._Native.WebviewPointerCallback;
import co.casterlabs.saucer._impl.ImplSaucerWebview._Native.WebviewSchemeCallback;
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
import co.casterlabs.saucer.utils.SaucerColor;
import co.casterlabs.saucer.utils.SaucerIcon;
import lombok.NonNull;

@SuppressWarnings("deprecation")
@JavascriptObject
class ImplSaucerWebview implements SaucerWebview {
    private static final _Native N = _SaucerNative.load(_Native.class);

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

    private WebviewPointerCallback webEventTitleChangedCallback = (Pointer _unused, Pointer $newTitle) -> {
        try {
            if (this.eventListener == null) return;
//            System.out.printf("titleChanged: %s\n", newUrl);
            String newTitle = $newTitle.getString(0, "UTF-8");
            this.eventListener.onTitleChanged(newTitle);
        } catch (Throwable t) {
            t.printStackTrace();
        }
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

    private WebviewPointerCallback webEventIconChangedCallback = (Pointer _unused, Pointer $newIcon) -> {
        try {
            if (this.eventListener == null) return;
            SaucerIcon newIcon = new SaucerIcon($newIcon);
//            System.out.printf("iconChanged: %s\n", newUrl);
            this.eventListener.onIconChanged(newIcon);
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

    private WebviewPointerCallback webEventUrlChangedCallback = (Pointer _unused, Pointer $newUrl) -> {
        try {
            if (this.eventListener == null) return;
//            System.out.printf("urlChanged: %s\n", newUrl);
            String newUrl = $newUrl.getString(0, "UTF-8");
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

        N.saucer_webview_on(this.saucer.$handle, _Native.SAUCER_WEB_EVENT_TITLE_CHANGED, this.webEventTitleChangedCallback);
        N.saucer_webview_on(this.saucer.$handle, _Native.SAUCER_WEB_EVENT_LOAD_FINISHED, this.webEventLoadFinishedCallback);
        N.saucer_webview_on(this.saucer.$handle, _Native.SAUCER_WEB_EVENT_ICON_CHANGED, this.webEventIconChangedCallback);
        N.saucer_webview_on(this.saucer.$handle, _Native.SAUCER_WEB_EVENT_LOAD_STARTED, this.webEventLoadStartedCallback);
        N.saucer_webview_on(this.saucer.$handle, _Native.SAUCER_WEB_EVENT_URL_CHANGED, this.webEventUrlChangedCallback);
        N.saucer_webview_on(this.saucer.$handle, _Native.SAUCER_WEB_EVENT_DOM_READY, this.webEventDomReadyCallback);

        N.saucer_webview_handle_scheme(this.saucer.$handle, _SafePointer.allocate(ImplSaucer.CUSTOM_SCHEME), this.schemeHandlerCallback);
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
        return $url.p().getString(0, "UTF-8");
    }

    @JavascriptSetter("currentUrl")
    @Override
    public void setUrl(@NonNull String url) {
        N.saucer_webview_set_url(this.saucer.$handle, _SafePointer.allocate(url));
    }

    @JavascriptFunction
    @Override
    public void serveScheme(@NonNull String path) {
        N.saucer_webview_serve_scheme(this.saucer.$handle, _SafePointer.allocate(path), _SafePointer.allocate(ImplSaucer.CUSTOM_SCHEME));
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

    @JavascriptGetter("background")
    @Override
    public SaucerColor getBackground() {
        _SafePointer $r = _SafePointer.allocate(1);
        _SafePointer $g = _SafePointer.allocate(1);
        _SafePointer $b = _SafePointer.allocate(1);
        _SafePointer $a = _SafePointer.allocate(1);

        N.saucer_webview_background(this.saucer.$handle, $r, $g, $b, $a);

        return new SaucerColor(
            $r.p().getByte(0),
            $g.p().getByte(0),
            $b.p().getByte(0),
            $a.p().getByte(0)
        );
    }

    @JavascriptSetter("background")
    @Override
    public void setBackground(@NonNull SaucerColor color) {
        N.saucer_webview_set_background(this.saucer.$handle, (byte) color.red(), (byte) color.green(), (byte) color.blue(), (byte) color.alpha());
    }

    @Override
    public SaucerIcon getFavicon() {
        Pointer icon = N.saucer_webview_favicon(this.saucer.$handle);
        return new SaucerIcon(icon);
    }

    @JavascriptGetter("forceDarkAppearance")
    @Override
    public boolean isForceDarkAppearance() {
        return N.saucer_webview_force_dark_mode(this.saucer.$handle);
    }

    @JavascriptSetter("forceDarkAppearance")
    @Override
    public void setForceDarkAppearance(boolean shouldAppearDark) {
        N.saucer_webview_set_force_dark_mode(this.saucer.$handle, shouldAppearDark);
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
        /** Requires {@link WebviewPointerCallback} */
        static final int SAUCER_WEB_EVENT_TITLE_CHANGED = 0;

        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WEB_EVENT_LOAD_FINISHED = 1;

        /** Requires {@link WebviewPointerCallback} */
        static final int SAUCER_WEB_EVENT_ICON_CHANGED = 2;

        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WEB_EVENT_LOAD_STARTED = 3;

        /** Requires {@link WebviewPointerCallback} */
        static final int SAUCER_WEB_EVENT_URL_CHANGED = 4;

        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WEB_EVENT_DOM_READY = 5;

        boolean saucer_webview_dev_tools(_SafePointer $saucer);

        void saucer_webview_set_dev_tools(_SafePointer $saucer, boolean setEnabled);

        _SafePointer saucer_webview_url(_SafePointer $saucer);

        void saucer_webview_set_url(_SafePointer $saucer, _SafePointer $url);

        void saucer_webview_serve_scheme(_SafePointer $saucer, _SafePointer $path, _SafePointer $scheme);

        boolean saucer_webview_context_menu(_SafePointer $saucer);

        void saucer_webview_set_context_menu(_SafePointer $saucer, boolean setEnabled);

        void saucer_webview_execute(_SafePointer $saucer, _SafePointer $script);

        boolean saucer_webview_background(_SafePointer $saucer, _SafePointer $r, _SafePointer $g, _SafePointer $b, _SafePointer $a);

        boolean saucer_webview_set_background(_SafePointer $saucer, byte r, byte g, byte b, byte a);

        Pointer saucer_webview_favicon(_SafePointer $saucer);

        boolean saucer_webview_force_dark_mode(_SafePointer $saucer);

        void saucer_webview_set_force_dark_mode(_SafePointer $saucer, boolean enabled);

        long saucer_webview_on(_SafePointer $saucer, int saucerWebEvent, Callback callback);

        void saucer_webview_handle_scheme(_SafePointer $saucer, _SafePointer $scheme, WebviewSchemeCallback callback);

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface WebviewPointerCallback extends Callback {
            void callback(Pointer $saucer, Pointer $str);
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
