package app.saucer._impl;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import app.saucer.SaucerWebview;
import app.saucer.SaucerWebview.SaucerWebviewListener.SaucerWebviewLoadState;
import app.saucer._impl.ImplSaucerWebview._Native.SAUCER_WEB_EVENT;
import app.saucer._impl.ImplSaucerWebview._Native.WebviewIconCallback;
import app.saucer._impl.ImplSaucerWebview._Native.WebviewLoadCallback;
import app.saucer._impl.ImplSaucerWebview._Native.WebviewNavigateCallback;
import app.saucer._impl.ImplSaucerWebview._Native.WebviewSchemeCallback;
import app.saucer._impl.ImplSaucerWebview._Native.WebviewStringCallback;
import app.saucer._impl.ImplSaucerWebview._Native.WebviewVoidCallback;
import app.saucer._impl.c.NoFree;
import app.saucer._impl.c.RequiresFree;
import app.saucer._impl.c._SaucerMemory;
import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptGetter;
import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptSetter;
import app.saucer.scheme.SaucerSchemeHandler;
import app.saucer.scheme.SaucerSchemeRequest;
import app.saucer.scheme.SaucerSchemeResponse;
import app.saucer.scheme.SaucerSchemeResponse.SaucerRequestError;
import app.saucer.utils.SaucerColor;
import app.saucer.utils.SaucerIcon;
import app.saucer.utils.SaucerNavigation;
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
            response = SaucerSchemeResponse.error(SaucerRequestError.NOT_FOUND);
        } else {
            try {
                response = this.schemeHandler.handle(request);
            } catch (Throwable t) {
                t.printStackTrace();
                response = SaucerSchemeResponse.error(SaucerRequestError.FAILED);
            }
        }

        response.disableAutoFree();
        return response;
    };

    private WebviewVoidCallback webEventDomReadyCallback = (Pointer _unused) -> {
        try {
            if (this.eventListener == null) return;
            this.eventListener.onDomReady();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WebviewStringCallback webEventNavigatedCallback = (Pointer _unused, Pointer $newUrl) -> {
        try {
            if (this.eventListener == null) return;
            String newTitle = $newUrl.getString(0, "UTF-8");
            this.eventListener.onTitle(newTitle);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WebviewNavigateCallback webEventNavigateCallback = (Pointer _unused, SaucerNavigation navigation) -> {
        try {
            if (this.eventListener == null) return 0;
            return this.eventListener.onNavigate(navigation) ? 0 : 1;
        } catch (Throwable t) {
            t.printStackTrace();
            return 0;
        }
    };

    private WebviewIconCallback webEventFaviconCallback = (Pointer _unused, SaucerIcon newIcon) -> {
        try {
            if (this.eventListener == null) return;
            this.eventListener.onFavicon(newIcon);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WebviewStringCallback webEventTitleCallback = (Pointer _unused, Pointer $newTitle) -> {
        try {
            if (this.eventListener == null) return;
            String newTitle = $newTitle.getString(0, "UTF-8");
            this.eventListener.onTitle(newTitle);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WebviewLoadCallback webEventLoadCallback = (Pointer _unused, Pointer $state) -> {
        try {
            if (this.eventListener == null) return;

            int state = $state.getInt(0);
            this.eventListener.onLoad(SaucerWebviewLoadState.values()[state]);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    ImplSaucerWebview(_ImplSaucer saucer) {
        this.saucer = saucer;

        N.saucer_webview_on(this.saucer, SAUCER_WEB_EVENT.DOM_READY.ordinal(), this.webEventDomReadyCallback);
        N.saucer_webview_on(this.saucer, SAUCER_WEB_EVENT.NAVIGATED.ordinal(), this.webEventNavigatedCallback);
        N.saucer_webview_on(this.saucer, SAUCER_WEB_EVENT.NAVIGATE.ordinal(), this.webEventNavigateCallback);
        N.saucer_webview_on(this.saucer, SAUCER_WEB_EVENT.FAVICON.ordinal(), this.webEventFaviconCallback);
        N.saucer_webview_on(this.saucer, SAUCER_WEB_EVENT.TITLE.ordinal(), this.webEventTitleCallback);
        N.saucer_webview_on(this.saucer, SAUCER_WEB_EVENT.LOAD.ordinal(), this.webEventLoadCallback);

        for (Pointer $scheme : _ImplSaucer.customSchemes.values()) {
            N.saucer_webview_handle_scheme(this.saucer, $scheme, this.schemeHandlerCallback);
        }
    }

    @JavascriptGetter("devtoolsVisible")
    @Override
    public boolean isDevtoolsVisible() {
        assert !this.saucer.isClosed : "This instance has been closed.";
        return N.saucer_webview_dev_tools(this.saucer);
    }

    @JavascriptSetter("devtoolsVisible")
    @Override
    public void setDevtoolsVisible(boolean show) {
        assert !this.saucer.isClosed : "This instance has been closed.";
        N.saucer_webview_set_dev_tools(this.saucer, show);
    }

    @JavascriptGetter("url")
    @Override
    public String currentUrl() {
        assert !this.saucer.isClosed : "This instance has been closed.";
        Pointer $url = N.saucer_webview_url(this.saucer);
        String url = $url.getString(0, "UTF-8");
        _SaucerMemory.free($url);
        return url;
    }

    @JavascriptSetter("url")
    @Override
    public void setUrl(@NonNull String url) {
        assert !this.saucer.isClosed : "This instance has been closed.";
        N.saucer_webview_set_url(this.saucer, url);
    }

    @JavascriptGetter("contextMenuAllowed")
    @Override
    public boolean isContextMenuAllowed() {
        assert !this.saucer.isClosed : "This instance has been closed.";
        return N.saucer_webview_context_menu(this.saucer);
    }

    @JavascriptSetter("contextMenuAllowed")
    @Override
    public void setContextMenuAllowed(boolean allowed) {
        assert !this.saucer.isClosed : "This instance has been closed.";
        N.saucer_webview_set_context_menu(this.saucer, allowed);
    }

    @JavascriptFunction
    @Override
    public void back() {
        assert !this.saucer.isClosed : "This instance has been closed.";
        N.saucer_webview_back(this.saucer);
    }

    @JavascriptFunction
    @Override
    public void forward() {
        assert !this.saucer.isClosed : "This instance has been closed.";
        N.saucer_webview_forward(this.saucer);
    }

    @JavascriptFunction
    @Override
    public void reload() {
        assert !this.saucer.isClosed : "This instance has been closed.";
        N.saucer_webview_reload(this.saucer);
    }

    @JavascriptGetter("background")
    @Override
    public SaucerColor getBackground() {
        assert !this.saucer.isClosed : "This instance has been closed.";
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
        assert !this.saucer.isClosed : "This instance has been closed.";
        N.saucer_webview_set_background(this.saucer, (byte) color.red(), (byte) color.green(), (byte) color.blue(), (byte) color.alpha());
    }

    @Override
    public SaucerIcon getFavicon() {
        assert !this.saucer.isClosed : "This instance has been closed.";
        return N.saucer_webview_favicon(this.saucer);
    }

    @JavascriptGetter("forceDarkAppearance")
    @Override
    public boolean isForceDarkAppearance() {
        assert !this.saucer.isClosed : "This instance has been closed.";
        return N.saucer_webview_force_dark_mode(this.saucer);
    }

    @JavascriptSetter("forceDarkAppearance")
    @Override
    public void setForceDarkAppearance(boolean shouldAppearDark) {
        assert !this.saucer.isClosed : "This instance has been closed.";
        N.saucer_webview_set_force_dark_mode(this.saucer, shouldAppearDark);
    }

    @Override
    public void setListener(@Nullable SaucerWebviewListener listener) {
        assert !this.saucer.isClosed : "This instance has been closed.";
        this.eventListener = listener;
    }

    @Override
    public void setSchemeHandler(@Nullable SaucerSchemeHandler handler) {
        assert !this.saucer.isClosed : "This instance has been closed.";
        this.schemeHandler = handler;
    }

    // https://github.com/saucer/bindings/blob/main/include/saucer/webview.h
    static interface _Native extends Library {
        static enum SAUCER_WEB_EVENT {
            /** Requires {@link WebviewVoidCallback} */
            DOM_READY,

            /** Requires {@link WebviewStringCallback} */
            NAVIGATED,

            /** Requires {@link WebviewNavigateCallback} */
            NAVIGATE,

            /** Requires {@link WebviewIconCallback} */
            FAVICON,

            /** Requires {@link WebviewStringCallback} */
            TITLE,

            /** Requires {@link WebviewLoadCallback} */
            LOAD,

        }

        void saucer_webview_back(_ImplSaucer saucer);

        void saucer_webview_forward(_ImplSaucer saucer);

        void saucer_webview_reload(_ImplSaucer saucer);

        @RequiresFree
        SaucerIcon saucer_webview_favicon(_ImplSaucer saucer);

        boolean saucer_webview_dev_tools(_ImplSaucer saucer);

        void saucer_webview_set_dev_tools(_ImplSaucer saucer, boolean setEnabled);

        @RequiresFree
        Pointer saucer_webview_url(_ImplSaucer saucer);

        void saucer_webview_set_url(_ImplSaucer saucer, String url);

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
        static interface WebviewNavigateCallback extends Callback {
            /**
             * 0 = allow, 1 = block
             */
            int callback(Pointer $saucer, SaucerNavigation nav);
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
        static interface WebviewLoadCallback extends Callback {
            void callback(Pointer $saucer, Pointer $state);
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
