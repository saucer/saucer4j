package co.casterlabs.saucer._impl;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.SaucerWebview;
import co.casterlabs.saucer._impl.ImplSaucerWebview._Native.WebviewIconCallback;
import co.casterlabs.saucer._impl.ImplSaucerWebview._Native.WebviewSchemeCallback;
import co.casterlabs.saucer._impl.ImplSaucerWebview._Native.WebviewStringCallback;
import co.casterlabs.saucer._impl.ImplSaucerWebview._Native.WebviewVoidCallback;
import co.casterlabs.saucer._impl.ImplSaucerWindow._Native.WindowVoidCallback;
import co.casterlabs.saucer._impl.c.NoFree;
import co.casterlabs.saucer._impl.c.RequiresFree;
import co.casterlabs.saucer._impl.c._SaucerMemory;
import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.bridge.JavascriptGetter;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.bridge.JavascriptSetter;
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

    private final _ImplSaucer saucer;

    private @Nullable SaucerWebviewListener eventListener;
    private @Nullable SaucerSchemeHandler schemeHandler;

    private WebviewSchemeCallback schemeHandlerCallback = (Pointer _unused, SaucerSchemeRequest request) -> {
        SaucerSchemeResponse response;
        if (this.schemeHandler == null) {
            response = SaucerSchemeResponse.error(SaucerRequestError.SAUCER_REQUEST_ERROR_NOT_FOUND);
        } else {
            try {
                response = this.schemeHandler.handle(request);
            } catch (Throwable t) {
                t.printStackTrace();
                response = SaucerSchemeResponse.error(SaucerRequestError.SAUCER_REQUEST_ERROR_FAILED);
            }
        }

        response.disableAutoFree();
        return response;
    };

    private WebviewStringCallback webEventTitleChangedCallback = (Pointer _unused, Pointer $newTitle) -> {
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

    private WebviewIconCallback webEventIconChangedCallback = (Pointer _unused, SaucerIcon newIcon) -> {
        try {
            if (this.eventListener == null) return;
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

    private WebviewStringCallback webEventUrlChangedCallback = (Pointer _unused, Pointer $newUrl) -> {
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

    ImplSaucerWebview(_ImplSaucer saucer) {
        this.saucer = saucer;

        N.saucer_webview_on(this.saucer, _Native.SAUCER_WEB_EVENT_TITLE_CHANGED, this.webEventTitleChangedCallback);
        N.saucer_webview_on(this.saucer, _Native.SAUCER_WEB_EVENT_LOAD_FINISHED, this.webEventLoadFinishedCallback);
        N.saucer_webview_on(this.saucer, _Native.SAUCER_WEB_EVENT_ICON_CHANGED, this.webEventIconChangedCallback);
        N.saucer_webview_on(this.saucer, _Native.SAUCER_WEB_EVENT_LOAD_STARTED, this.webEventLoadStartedCallback);
        N.saucer_webview_on(this.saucer, _Native.SAUCER_WEB_EVENT_URL_CHANGED, this.webEventUrlChangedCallback);
        N.saucer_webview_on(this.saucer, _Native.SAUCER_WEB_EVENT_DOM_READY, this.webEventDomReadyCallback);

        N.saucer_webview_handle_scheme(this.saucer, _ImplSaucer.CUSTOM_SCHEME, this.schemeHandlerCallback);
    }

    @JavascriptGetter("devtoolsVisible")
    @Override
    public boolean isDevtoolsVisible() {
        return N.saucer_webview_dev_tools(this.saucer);
    }

    @JavascriptSetter("devtoolsVisible")
    @Override
    public void setDevtoolsVisible(boolean show) {
        N.saucer_webview_set_dev_tools(this.saucer, show);
    }

    @JavascriptGetter("currentUrl")
    @Override
    public String currentUrl() {
        Pointer $url = N.saucer_webview_url(this.saucer);
        String url = $url.getString(0, "UTF-8");
        _SaucerMemory.free($url);
        return url;
    }

    @JavascriptSetter("currentUrl")
    @Override
    public void setUrl(@NonNull String url) {
        N.saucer_webview_set_url(this.saucer, url);
    }

    @JavascriptFunction
    @Override
    public void serveScheme(@NonNull String path) {
        N.saucer_webview_serve_scheme(this.saucer, path, _ImplSaucer.CUSTOM_SCHEME);
    }

    @JavascriptGetter("contextMenuAllowed")
    @Override
    public boolean isContextMenuAllowed() {
        return N.saucer_webview_context_menu(this.saucer);
    }

    @JavascriptSetter("contextMenuAllowed")
    @Override
    public void setContextMenuAllowed(boolean allowed) {
        N.saucer_webview_set_context_menu(this.saucer, allowed);
    }

    @JavascriptFunction
    @Override
    public void executeJavaScript(@NonNull String scriptToExecute) {
        N.saucer_webview_execute(this.saucer, '{' + scriptToExecute + '}');
    }

    @JavascriptGetter("background")
    @Override
    public SaucerColor getBackground() {
        Pointer $r = _SaucerMemory.alloc(1);
        Pointer $g = _SaucerMemory.alloc(1);
        Pointer $b = _SaucerMemory.alloc(1);
        Pointer $a = _SaucerMemory.alloc(1);

        N.saucer_webview_background(this.saucer, $r, $g, $b, $a);

        SaucerColor result = new SaucerColor(
            $r.getByte(0),
            $g.getByte(0),
            $b.getByte(0),
            $a.getByte(0)
        );

        _SaucerMemory.free($r);
        _SaucerMemory.free($g);
        _SaucerMemory.free($b);
        _SaucerMemory.free($a);

        return result;
    }

    @JavascriptSetter("background")
    @Override
    public void setBackground(@NonNull SaucerColor color) {
        N.saucer_webview_set_background(this.saucer, (byte) color.red(), (byte) color.green(), (byte) color.blue(), (byte) color.alpha());
    }

    @Override
    public SaucerIcon getFavicon() {
        return N.saucer_webview_favicon(this.saucer);
    }

    @JavascriptGetter("forceDarkAppearance")
    @Override
    public boolean isForceDarkAppearance() {
        return N.saucer_webview_force_dark_mode(this.saucer);
    }

    @JavascriptSetter("forceDarkAppearance")
    @Override
    public void setForceDarkAppearance(boolean shouldAppearDark) {
        N.saucer_webview_set_force_dark_mode(this.saucer, shouldAppearDark);
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
        /** Requires {@link WebviewStringCallback} */
        static final int SAUCER_WEB_EVENT_TITLE_CHANGED = 0;

        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WEB_EVENT_LOAD_FINISHED = 1;

        /** Requires {@link WebviewIconCallback} */
        static final int SAUCER_WEB_EVENT_ICON_CHANGED = 2;

        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WEB_EVENT_LOAD_STARTED = 3;

        /** Requires {@link WebviewStringCallback} */
        static final int SAUCER_WEB_EVENT_URL_CHANGED = 4;

        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WEB_EVENT_DOM_READY = 5;

        void saucer_webview_execute(_ImplSaucer saucer, String script);

        @RequiresFree
        SaucerIcon saucer_webview_favicon(_ImplSaucer saucer);

        boolean saucer_webview_dev_tools(_ImplSaucer saucer);

        void saucer_webview_set_dev_tools(_ImplSaucer saucer, boolean setEnabled);

        @RequiresFree
        Pointer saucer_webview_url(_ImplSaucer saucer);

        void saucer_webview_set_url(_ImplSaucer saucer, String url);

        void saucer_webview_serve_scheme(_ImplSaucer saucer, String path, Pointer $scheme);

        boolean saucer_webview_context_menu(_ImplSaucer saucer);

        void saucer_webview_set_context_menu(_ImplSaucer saucer, boolean setEnabled);

        boolean saucer_webview_background(_ImplSaucer saucer, Pointer $r, Pointer $g, Pointer $b, Pointer $a);

        boolean saucer_webview_set_background(_ImplSaucer saucer, byte r, byte g, byte b, byte a);

        boolean saucer_webview_force_dark_mode(_ImplSaucer saucer);

        void saucer_webview_set_force_dark_mode(_ImplSaucer saucer, boolean enabled);

        long saucer_webview_on(_ImplSaucer saucer, int saucerWebEvent, Callback callback);

        void saucer_webview_handle_scheme(_ImplSaucer saucer, Pointer scheme, WebviewSchemeCallback callback);

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface WebviewStringCallback extends Callback {
            void callback(Pointer $saucer, Pointer $str);
        }

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface WebviewIconCallback extends Callback {
            void callback(Pointer $saucer, SaucerIcon icon);
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
            SaucerSchemeResponse callback(Pointer $saucer, @NoFree SaucerSchemeRequest request);
        }

    }

}
