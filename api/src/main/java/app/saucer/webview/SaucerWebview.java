package app.saucer.webview;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Callback;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

import app.saucer.SaucerApp;
import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptGetter;
import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptSetter;
import app.saucer.ntv.ntv_app.saucer_policy;
import app.saucer.ntv.ntv_icon.saucer_icon;
import app.saucer.ntv.ntv_navigation.saucer_navigation;
import app.saucer.ntv.ntv_scheme;
import app.saucer.ntv.ntv_scheme.saucer_scheme_error;
import app.saucer.ntv.ntv_scheme.saucer_scheme_executor;
import app.saucer.ntv.ntv_scheme.saucer_scheme_handler;
import app.saucer.ntv.ntv_scheme.saucer_scheme_request;
import app.saucer.ntv.ntv_url.saucer_url;
import app.saucer.ntv.ntv_webview;
import app.saucer.ntv.ntv_webview.saucer_webview;
import app.saucer.ntv.ntv_webview.saucer_webview_event;
import app.saucer.ntv.ntv_webview.saucer_webview_event_dom_ready;
import app.saucer.ntv.ntv_webview.saucer_webview_event_favicon;
import app.saucer.ntv.ntv_webview.saucer_webview_event_load;
import app.saucer.ntv.ntv_webview.saucer_webview_event_navigate;
import app.saucer.ntv.ntv_webview.saucer_webview_event_navigated;
import app.saucer.ntv.ntv_webview.saucer_webview_event_title;
import app.saucer.ntv.documentation.BeforeInit;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.size_t;
import app.saucer.util.SaucerColor;
import app.saucer.util.SaucerRectangle;
import app.saucer.util.SaucerUrl;
import app.saucer.webview.bridge.SaucerBridge;
import app.saucer.webview.bridge.SaucerMessages;
import app.saucer.webview.scheme.SaucerSchemeHandler;
import app.saucer.webview.scheme.SaucerSchemeRequest;
import app.saucer.webview.scheme.SaucerSchemeResponse;
import app.saucer.webview.window.SaucerIcon;
import app.saucer.webview.window.SaucerWindow;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @apiNote This class is not thread-safe. You must call it from the main thread
 *          or use {@link SaucerApp#dispatch(Runnable)} or
 *          {@link SaucerApp#dispatch(Supplier)}.
 */
@SuppressWarnings("deprecation")
@JavascriptObject
@Accessors(fluent = true, chain = true)
public final class SaucerWebview extends SaucerBoxedType<saucer_webview> {
    private static boolean alreadyLoaded = false;

    private static final Set<String> customSchemes = new HashSet<>();

    public final SaucerWindow window;
    public final SaucerBridge bridge;
    public final SaucerMessages messages;

    private @Setter @Nullable SaucerWebviewListener listener;

    private @Getter boolean isClosed = false;
    private final Runnable onClose;

    private Map<String, saucer_scheme_handler> schemeHandlers = new HashMap<>();

    // TODO saucer_webview_event_permission
    // TODO saucer_webview_event_fullscreen

    private final saucer_webview_event_dom_ready domReadyCallback = (saucer_webview _unused, Callback _unused2) -> {
        if (this.listener != null) {
            this.listener.onDomReady();
        }
    };

    private final saucer_webview_event_navigated navigatedCallback = (saucer_webview _unused, saucer_url url, Callback _unused2) -> {
        if (this.listener != null) {
            SaucerUrl boxed = new SaucerUrl(url, false);
            SaucerBoxedType.noFree(boxed); // Prevent double-free.
            this.listener.onNavigated(boxed);
        }
    };

    private final saucer_webview_event_navigate navigateCallback = (saucer_webview _unused, saucer_navigation nav, Callback _unused2) -> {
        if (this.listener != null) {
            SaucerNavigation boxed = new SaucerNavigation(nav, false);
            if (!this.listener.onNavigate(boxed)) {
                return saucer_policy.BLOCK;
            }
        }

        return saucer_policy.ALLOW;
    };

    // saucer_webview_event_message is handled in SaucerBridge.
    // TODO saucer_webview_event_request

    private final saucer_webview_event_favicon faviconCallback = (saucer_webview _unused, saucer_icon icon, Callback _unused2) -> {
        if (this.listener != null) {
            SaucerIcon boxed = new SaucerIcon(icon, false);
            this.listener.onFavicon(boxed);
        }
    };

    private final saucer_webview_event_title titleCallback = (saucer_webview _unused, String title, size_t arg2, Callback _unused2) -> {
        if (this.listener != null) {
            this.listener.onTitle(title);
        }
    };

    private final saucer_webview_event_load loadCallback = (saucer_webview _unused, /*saucer_state*/int arg1, Callback _unused2) -> {
        if (this.listener != null) {
            SaucerWebviewLoadState state = SaucerWebviewLoadState.LUT[arg1];
            this.listener.onLoad(state);
        }
    };

    /**
     * @deprecated Native interop only.
     * 
     * @implNote   This class does not free() itself automatically, which differs
     *             from most BoxedTypes.
     */
    @Deprecated
    public SaucerWebview(saucer_webview webview, SaucerWindow window, Runnable onClose) {
        super(webview, false);
        alreadyLoaded = true;

        this.window = window;
        this.bridge = new SaucerBridge(this);
        this.messages = new SaucerMessages(this);

        this.onClose = onClose;

//        ntv_webview.N.saucer_webview_on($ref, saucer_webview_event.PERMISSION, this.permissionCallback, false, null);
//        ntv_webview.N.saucer_webview_on($ref, saucer_webview_event.FULLSCREEN, this.fullscreenCallback, false, null);
        ntv_webview.N.saucer_webview_on($ref, saucer_webview_event.DOM_READY, this.domReadyCallback, false, null);
        ntv_webview.N.saucer_webview_on($ref, saucer_webview_event.NAVIGATED, this.navigatedCallback, false, null);
        ntv_webview.N.saucer_webview_on($ref, saucer_webview_event.NAVIGATE, this.navigateCallback, false, null);
//        ntv_webview.N.saucer_webview_on($ref, saucer_webview_event.REQUEST, this.requestCallback, false, null);
        ntv_webview.N.saucer_webview_on($ref, saucer_webview_event.FAVICON, this.faviconCallback, false, null);
        ntv_webview.N.saucer_webview_on($ref, saucer_webview_event.TITLE, this.titleCallback, false, null);
        ntv_webview.N.saucer_webview_on($ref, saucer_webview_event.LOAD, this.loadCallback, false, null);
    }

    /**
     * Frees the webview and its resources. Additionally, the webview will be
     * removed from it's parent window.
     */
    @JavascriptFunction
    public void destroy() {
        if (this.isClosed) return;
        this.isClosed = true;

        this.onClose.run();
        $ref.close();
    }

    @BeforeInit
    public static void registerCustomScheme(@NonNull String scheme) {
        assert !alreadyLoaded : "You must register all of your custom schemes before calling SaucerWebview.create()";

        if (customSchemes.add(scheme)) {
            ntv_webview.N.saucer_webview_register_scheme(scheme);
        }
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * @return the current URL the webview is navigated to.
     */
    @JavascriptGetter("url")
    public SaucerUrl url() {
        IntByReference error = new IntByReference(0);
        saucer_url $url = ntv_webview.N.saucer_webview_url($ref, error);

        if (error.getValue() != 0) {
            throw new IllegalStateException("Failed to get current URL (error code " + error.getValue() + ")");
        }

        return new SaucerUrl($url, true);
    }

    /**
     * Navigates the webview to the given URL.
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("url")
    public SaucerWebview url(@NonNull SaucerUrl url) {
        ntv_webview.N.saucer_webview_set_url($ref, SaucerBoxedType.ntv(url));
        return this;
    }

    /**
     * @return the current favicon of the webview.
     */
    public SaucerIcon favicon() {
        saucer_icon $icon = ntv_webview.N.saucer_webview_favicon($ref);
        return new SaucerIcon($icon, true);
    }

    /**
     * @return the current title of the webview.
     */
    @JavascriptGetter("title")
    public String title() {
        size_t.ByReference sizeRef = new size_t.ByReference();

        // First call to get the size
        ntv_webview.N.saucer_webview_page_title($ref, null, sizeRef);

        // Second call to get the actual string
        byte[] buffer = new byte[sizeRef.getValue().intValue()];
        ntv_webview.N.saucer_webview_page_title($ref, buffer, sizeRef);

        return new String(buffer, StandardCharsets.UTF_8);
    }

    /**
     * @return true if the dev tools are visible.
     */
    @JavascriptGetter("devtoolsVisible")
    public boolean isDevToolsVisible() {
        return ntv_webview.N.saucer_webview_dev_tools($ref);
    }

    /**
     * Sets whether the dev tools are visible.
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("devtoolsVisible")
    public SaucerWebview devToolsVisible(boolean visible) {
        ntv_webview.N.saucer_webview_set_dev_tools($ref, visible);
        return this;
    }

    /**
     * @return true if the context menu is allowed.
     */
    @JavascriptGetter("contextMenu")
    public boolean isContextMenuAllowed() {
        return ntv_webview.N.saucer_webview_context_menu($ref);
    }

    /**
     * Sets whether the context menu is allowed.
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("contextMenu")
    public SaucerWebview contextMenuAllowed(boolean enabled) {
        ntv_webview.N.saucer_webview_set_context_menu($ref, enabled);
        return this;
    }

    /**
     * @return true if the `prefers-color-scheme` media query is forcibly set to
     *         `dark`.
     */
    @JavascriptGetter("forceDark")
    public boolean isForceDarkEnabled() {
        return ntv_webview.N.saucer_webview_force_dark($ref);
    }

    /**
     * Forces the `prefers-color-scheme` media query to `dark` if true.
     * 
     * @return   this instance, for chaining.
     * 
     * @implNote For Qt6, a version of 6.7 or greater is required for this to work.
     */
    @JavascriptSetter("forceDark")
    public SaucerWebview forceDarkEnabled(boolean enabled) {
        ntv_webview.N.saucer_webview_set_force_dark($ref, enabled);
        return this;
    }

    /**
     * @return the background color of the webview.
     */
    @JavascriptGetter("backgroundColor")
    public SaucerColor backgroundColor() {
        ByteByReference rRef = new ByteByReference();
        ByteByReference gRef = new ByteByReference();
        ByteByReference bRef = new ByteByReference();
        ByteByReference aRef = new ByteByReference();

        ntv_webview.N.saucer_webview_background($ref, rRef, gRef, bRef, aRef);

        return new SaucerColor(
            Byte.toUnsignedInt(rRef.getValue()),
            Byte.toUnsignedInt(gRef.getValue()),
            Byte.toUnsignedInt(bRef.getValue()),
            Byte.toUnsignedInt(aRef.getValue())
        );
    }

    /**
     * Sets the background color of the webview.
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("backgroundColor")
    public SaucerWebview backgroundColor(@NonNull SaucerColor color) {
        ntv_webview.N.saucer_webview_set_background(
            $ref,
            (byte) color.red,
            (byte) color.green,
            (byte) color.blue,
            (byte) color.alpha
        );
        return this;
    }

    /**
     * @return the bounds of the webview within its parent window.
     */
    @JavascriptGetter("bounds")
    public SaucerRectangle bounds() {
        IntByReference xRef = new IntByReference();
        IntByReference yRef = new IntByReference();
        IntByReference wRef = new IntByReference();
        IntByReference hRef = new IntByReference();

        ntv_webview.N.saucer_webview_bounds($ref, xRef, yRef, wRef, hRef);

        return new SaucerRectangle(
            xRef.getValue(),
            yRef.getValue(),
            wRef.getValue(),
            hRef.getValue()
        );
    }

    /**
     * Sets the bounds of the webview within its parent window. If null, the webview
     * will be set to fill the entire window.
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("bounds")
    public SaucerWebview bounds(@Nullable SaucerRectangle bounds) {
        if (bounds == null) {
            ntv_webview.N.saucer_webview_reset_bounds($ref);
            return this;
        }

        ntv_webview.N.saucer_webview_set_bounds(
            $ref,
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        );
        return this;
    }

    /**
     * Navigates back in the webview's history.
     * 
     * @return this instance, for chaining.
     */
    @JavascriptFunction("back")
    public SaucerWebview back() {
        ntv_webview.N.saucer_webview_back($ref);
        return this;
    }

    /**
     * Navigates forward in the webview's history.
     * 
     * @return this instance, for chaining.
     */
    @JavascriptFunction("forward")
    public SaucerWebview forward() {
        ntv_webview.N.saucer_webview_forward($ref);
        return this;
    }

    /**
     * Reloads the current page.
     * 
     * @return this instance, for chaining.
     */
    @JavascriptFunction("reload")
    public SaucerWebview reload() {
        ntv_webview.N.saucer_webview_reload($ref);
        return this;
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * Allows you to serve custom webpages from your own resources.
     * 
     * @param  name the scheme name to handle. Must have been registered via
     *              {@link #registerCustomScheme(String)} before any webviews were
     *              created.
     * 
     * @return      this instance, for chaining.
     */
    public SaucerWebview addSchemeHandler(@NonNull String name, @NonNull SaucerSchemeHandler handler) {
        assert !this.schemeHandlers.containsKey(name) : "You can only add one handler per scheme. Did you mean to call removeSchemeHandler() first?";
        assert customSchemes.contains(name) : "You must register your scheme via Saucer.registerCustomScheme()";
        saucer_scheme_handler handlerNtv = new SaucerSchemeWrapper(handler);
        ntv_webview.N.saucer_webview_handle_scheme($ref, name, handlerNtv);
        this.schemeHandlers.put(name, handlerNtv);
        return this;
    }

    /**
     * Removes a previously added scheme handler.
     * 
     * @param  name the scheme name to stop handling.
     * 
     * @return      this instance, for chaining.
     */
    public SaucerWebview removeSchemeHandler(@NonNull String name, @NonNull SaucerSchemeHandler handler) {
        if (!this.schemeHandlers.containsKey(name)) return this; // Silently fail.
        ntv_webview.N.saucer_webview_remove_scheme($ref, name);
        this.schemeHandlers.remove(name);
        return this;
    }

    @RequiredArgsConstructor
    private static class SaucerSchemeWrapper implements saucer_scheme_handler {
        private final SaucerSchemeHandler handler;

        @Override
        public void callback(saucer_scheme_request req, saucer_scheme_executor exec) {
            try {
                SaucerSchemeRequest boxedReq = new SaucerSchemeRequest(req, false);
                SaucerSchemeResponse boxedRes = this.handler.handle(boxedReq); // Saucer copies the data internally, so we let the GC clean this up.

                if (boxedRes == null) {
                    ntv_scheme.N.saucer_scheme_executor_reject(exec, saucer_scheme_error.INVALID);
                } else {
                    ntv_scheme.N.saucer_scheme_executor_accept(exec, SaucerBoxedType.ntv(boxedRes));
                }
            } catch (Throwable t) {
                t.printStackTrace();
                ntv_scheme.N.saucer_scheme_executor_reject(exec, saucer_scheme_error.FAILED);
            }
        }

    }

}
