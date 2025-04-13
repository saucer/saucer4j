package app.saucer.webview;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import app.saucer.Saucer;
import app.saucer.SaucerApp;
import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptGetter;
import app.saucer.bridge.JavascriptSetter;
import app.saucer.ntv._icon.saucer_icon;
import app.saucer.ntv._memory;
import app.saucer.ntv._scheme;
import app.saucer.ntv._scheme.SAUCER_SCHEME_ERROR;
import app.saucer.ntv._scheme.saucer_scheme_executor;
import app.saucer.ntv._scheme.saucer_scheme_handler;
import app.saucer.ntv._scheme.saucer_scheme_request;
import app.saucer.ntv._webview;
import app.saucer.ntv._webview.SAUCER_LAUNCH;
import app.saucer.ntv._webview.SAUCER_WEB_EVENT;
import app.saucer.ntv._webview.SAUCER_WEB_EVENT.WebviewDomReadyCallback;
import app.saucer.ntv._webview.SAUCER_WEB_EVENT.WebviewFavIconCallback;
import app.saucer.ntv._webview.SAUCER_WEB_EVENT.WebviewLoadCallback;
import app.saucer.ntv._webview.SAUCER_WEB_EVENT.WebviewNavigateCallback;
import app.saucer.ntv._webview.SAUCER_WEB_EVENT.WebviewNavigatedCallback;
import app.saucer.ntv._webview.SAUCER_WEB_EVENT.WebviewTitleCallback;
import app.saucer.ntv._window.SAUCER_POLICY;
import app.saucer.ntv._window.saucer_handle;
import app.saucer.ntv.documentation.RequiresFree;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.SaucerPointerReference;
import app.saucer.ntv.util.size_t;
import app.saucer.util.SaucerColor;
import app.saucer.webview.SaucerWebviewListener.SaucerWebviewLoadState;
import app.saucer.webview.scheme.SaucerSchemeHandler;
import app.saucer.webview.scheme.SaucerSchemeRequest;
import app.saucer.webview.scheme.SaucerSchemeResponse;
import app.saucer.webview.window.SaucerIcon;
import app.saucer.webview.window.SaucerWindow;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @apiNote This class is not thread-safe. You must call it from the main thread
 *          or use {@link SaucerApp#dispatch(Runnable)} or
 *          {@link SaucerApp#dispatch(Supplier)}.
 */
@SuppressWarnings("deprecation")
public class SaucerWebview {
    private final Saucer saucer;

    private @Setter @Nullable SaucerWebviewListener listener;

    private Map<String, saucer_scheme_handler> schemeHandlers = new HashMap<>();

    private final WebviewDomReadyCallback domReadyCallback = (_unused) -> {
        if (this.listener != null) {
            this.listener.onDomReady();
        }
    };

    private final WebviewNavigatedCallback navigatedCallback = (_unused, url) -> {
        if (this.listener != null) {
            this.listener.onNavigated(url);
        }
    };

    private final WebviewNavigateCallback navigateCallback = (_unused, nav) -> {
        if (this.listener != null) {
            SaucerNavigation boxed = new SaucerNavigation(nav);
            if (!this.listener.onNavigate(boxed)) {
                return SAUCER_POLICY.BLOCK;
            }
        }

        return SAUCER_POLICY.ALLOW;
    };

    private final WebviewFavIconCallback faviconCallback = (_unused, icon) -> {
        if (this.listener != null) {
            SaucerIcon boxed = new SaucerIcon(icon);
            this.listener.onFavicon(boxed);
        }
    };

    private final WebviewTitleCallback titleCallback = (_unused, title) -> {
        if (this.listener != null) {
            this.listener.onTitle(title);
        }
    };

    private final WebviewLoadCallback loadCallback = (_unused, $state) -> {
        try ($state) {
            if (this.listener != null) {
                SaucerWebviewLoadState state = SaucerWebviewLoadState.values()[$state.asInt()];
                this.listener.onLoad(state);
            }
        }
    };

    /**
     * @deprecated Native interop only.
     * 
     * @implNote   This class does not free() itself automatically, which differs
     *             from most BoxedTypes.
     */
    @Deprecated
    public SaucerWebview(Saucer saucer) {
        this.saucer = saucer;

        _webview.N.saucer_webview_on(this.saucer.ntv(), SAUCER_WEB_EVENT.DOM_READY, this.domReadyCallback);
        _webview.N.saucer_webview_on(this.saucer.ntv(), SAUCER_WEB_EVENT.NAVIGATED, this.navigatedCallback);
        _webview.N.saucer_webview_on(this.saucer.ntv(), SAUCER_WEB_EVENT.NAVIGATE, this.navigateCallback);
        _webview.N.saucer_webview_on(this.saucer.ntv(), SAUCER_WEB_EVENT.FAVICON, this.faviconCallback);
        _webview.N.saucer_webview_on(this.saucer.ntv(), SAUCER_WEB_EVENT.TITLE, this.titleCallback);
        _webview.N.saucer_webview_on(this.saucer.ntv(), SAUCER_WEB_EVENT.LOAD, this.loadCallback);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * @return whether or not the devtools window is open.
     */
    @JavascriptGetter("devtoolsVisible")
    public boolean isDevtoolsVisible() {
        return _webview.N.saucer_webview_dev_tools(this.saucer.ntv());
    }

    /**
     * Sets whether or not the devtools window should be shown.
     */
    @JavascriptSetter("devtoolsVisible")
    public void setDevtoolsVisible(boolean show) {
        _webview.N.saucer_webview_set_dev_tools(this.saucer.ntv(), show);
    }

    /**
     * @return the current URL the webview is navigated to.
     */
    @JavascriptGetter("url")
    public String currentUrl() {
        try (SaucerPointerReference<String> ref = _webview.N.saucer_webview_url(this.saucer.ntv())) {
            return ref.asString();
        }
    }

    /**
     * Navigates the webview to the given URL.
     */
    @JavascriptSetter("url")
    public void setUrl(@NonNull String url) {
        _webview.N.saucer_webview_set_url(this.saucer.ntv(), url);
    }

    /**
     * @return whether or not the context menu is enabled.
     */
    @JavascriptGetter("contextMenuAllowed")
    public boolean isContextMenuAllowed() {
        return _webview.N.saucer_webview_context_menu(this.saucer.ntv());
    }

    /**
     * Enables the default context menu (i.e right-click menu).
     */
    @JavascriptSetter("contextMenuAllowed")
    public void setContextMenuAllowed(boolean allowed) {
        _webview.N.saucer_webview_set_context_menu(this.saucer.ntv(), allowed);
    }

    /**
     * Navigates backward.
     */
    @JavascriptFunction
    public void back() {
        _webview.N.saucer_webview_back(this.saucer.ntv());
    }

    /**
     * Navigates forward.
     */
    @JavascriptFunction
    public void forward() {
        _webview.N.saucer_webview_forward(this.saucer.ntv());
    }

    /**
     * Reloads the current page.
     */
    @JavascriptFunction
    public void reload() {
        _webview.N.saucer_webview_reload(this.saucer.ntv());
    }

    /**
     * @return the background color of the Saucer window. Note that this is
     *         different from the HTML background.
     */
    public SaucerColor getBackground() {
        try (
            SaucerPointerReference<Byte> rRef = _memory.N.saucer_memory_alloc(new size_t(Byte.BYTES));
            SaucerPointerReference<Byte> gRef = _memory.N.saucer_memory_alloc(new size_t(Byte.BYTES));
            SaucerPointerReference<Byte> bRef = _memory.N.saucer_memory_alloc(new size_t(Byte.BYTES));
            SaucerPointerReference<Byte> aRef = _memory.N.saucer_memory_alloc(new size_t(Byte.BYTES));) {
            _webview.N.saucer_webview_background(this.saucer.ntv(), rRef, gRef, bRef, aRef);
            return new SaucerColor(rRef.asByte(), gRef.asByte(), bRef.asByte(), aRef.asByte());
        }
    }

    /**
     * Allows you to set the background color of the Saucer window. Note that this
     * is different from the HTML background.
     * 
     * This is useful if you want a transparent or translucent application.
     * 
     * @implNote Applying blur effects in HTML will not cause the apps behind the
     *           window to appear blurred.
     */
    public void setBackground(@NonNull SaucerColor color) {
        _webview.N.saucer_webview_set_background(
            this.saucer.ntv(),
            (byte) color.red, (byte) color.green, (byte) color.blue, (byte) color.alpha
        );
    }

    /**
     * Gets the current favicon of the loaded page. Useful with
     * {@link SaucerWindow#setIcon(SaucerIcon)}.
     */
    public SaucerIcon getFavicon() {
        saucer_icon iconNtv = _webview.N.saucer_webview_favicon(this.saucer.ntv());
        return new SaucerIcon(iconNtv);
    }

    /**
     * @return true if the `prefers-color-scheme` media query is forcibly set to
     *         `dark`.
     */
    @JavascriptGetter("forceDarkAppearance")
    public boolean isForceDarkAppearance() {
        return _webview.N.saucer_webview_force_dark_mode(this.saucer.ntv());
    }

    /**
     * Forces the `prefers-color-scheme` media query to `dark` if true.
     * 
     * @implNote The Qt5 backend does not support this, so the call will do nothing
     *           on the Qt5 backend.
     * @implNote For Qt6, a version of 6.7 or greater is required for this to work.
     */
    @JavascriptSetter("forceDarkAppearance")
    public void setForceDarkAppearance(boolean shouldAppearDark) {
        _webview.N.saucer_webview_set_force_dark_mode(this.saucer.ntv(), shouldAppearDark);
    }

    /**
     * Allows you to serve custom webpages from your own resources.
     */
    public void addSchemeHandler(@NonNull String name, @NonNull SaucerSchemeHandler handler) {
        assert !this.schemeHandlers.containsKey(name) : "You can only add one handler per scheme. Did you mean to call removeSchemeHandler() first?";
        assert Saucer.registeredSchemes.contains(name) : "You must register your scheme via Saucer.registerCustomScheme()";
        saucer_scheme_handler handlerNtv = new SaucerSchemeWrapper(handler);
        _webview.N.saucer_webview_handle_scheme(this.saucer.ntv(), name, handlerNtv, SAUCER_LAUNCH.ASYNC);
        this.schemeHandlers.put(name, handlerNtv);
    }

    public void removeSchemeHandler(@NonNull String name, @NonNull SaucerSchemeHandler handler) {
        if (!this.schemeHandlers.containsKey(name)) return; // Silently fail.
        _webview.N.saucer_webview_remove_scheme(this.saucer.ntv(), name);
        this.schemeHandlers.remove(name);
    }

//    /**
//     * Allows you to receive events from the Webview, such as page load or
//     * navigation events.
//     */
//    public void setListener(@Nullable SaucerWebviewListener listener);

//    public static interface SaucerWebviewListener {
//
//        default void onDomReady() {}
//
//        default void onNavigated(String newUrl) {}
//
//        /**
//         * @return true, if the navigation should be handled normally.
//         */
//        default boolean onNavigate(SaucerNavigation navigation) {
//            return true;
//        }
//
//        default void onFavicon(SaucerIcon newIcon) {}
//
//        default void onTitle(String newTitle) {}
//
//        default void onLoad(SaucerWebviewLoadState state) {}
//
//        public static enum SaucerWebviewLoadState {
//            STARTED,
//            FINISHED
//        }
//
//    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    @RequiredArgsConstructor
    private static class SaucerSchemeWrapper implements saucer_scheme_handler {
        private final SaucerSchemeHandler handler;

        @Override
        public void callback(saucer_handle saucer, @RequiresFree saucer_scheme_request req, @RequiresFree saucer_scheme_executor exec) {
            try {
                SaucerSchemeRequest boxedReq = new SaucerSchemeRequest(req); // This is free()'d by the garbage collector.
                SaucerSchemeResponse boxedRes = this.handler.handle(boxedReq); // This is free()'d by the garbage collector.

                if (boxedRes == null) {
                    _scheme.N.saucer_scheme_executor_reject(exec, SAUCER_SCHEME_ERROR.ABORTED);
                } else {
                    _scheme.N.saucer_scheme_executor_resolve(exec, SaucerBoxedType.ntv(boxedRes));
                }
            } catch (Throwable t) {
                t.printStackTrace();
                _scheme.N.saucer_scheme_executor_reject(exec, SAUCER_SCHEME_ERROR.FAILED);
            } finally {
//                req.close();
                exec.close();
            }
        }

    }

}
