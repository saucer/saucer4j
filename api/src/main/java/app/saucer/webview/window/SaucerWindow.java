package app.saucer.webview.window;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Callback;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

import app.saucer.SaucerApp;
import app.saucer.SaucerScreen;
import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptGetter;
import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptSetter;
import app.saucer.ntv.ntv_app.saucer_policy;
import app.saucer.ntv.ntv_app.saucer_screen;
import app.saucer.ntv.ntv_webview;
import app.saucer.ntv.ntv_webview.saucer_webview;
import app.saucer.ntv.ntv_webview.saucer_webview_options;
import app.saucer.ntv.ntv_window;
import app.saucer.ntv.ntv_window.saucer_window;
import app.saucer.ntv.ntv_window.saucer_window_event;
import app.saucer.ntv.ntv_window.saucer_window_event_close;
import app.saucer.ntv.ntv_window.saucer_window_event_closed;
import app.saucer.ntv.ntv_window.saucer_window_event_decorated;
import app.saucer.ntv.ntv_window.saucer_window_event_focus;
import app.saucer.ntv.ntv_window.saucer_window_event_maximize;
import app.saucer.ntv.ntv_window.saucer_window_event_minimize;
import app.saucer.ntv.ntv_window.saucer_window_event_resize;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.size_t;
import app.saucer.util.SaucerColor;
import app.saucer.util.SaucerPosition;
import app.saucer.util.SaucerSize;
import app.saucer.webview.SaucerWebview;
import app.saucer.webview.SaucerWebviewOptions;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * @apiNote This class is not thread-safe. You must call it from the main thread
 *          or use {@link SaucerApp#dispatch(Runnable)} or
 *          {@link SaucerApp#dispatch(Supplier)}.
 */
@JavascriptObject
@SuppressWarnings("deprecation")
public final class SaucerWindow extends SaucerBoxedType<saucer_window> {
    private static final Set<SaucerWindow> instances = new HashSet<>();

    private final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor();
    private final Set<SaucerWebview> webviews = new HashSet<>();

    private @Setter @Nullable SaucerWindowListener listener;
    private @Getter boolean isClosed = false;

    private final saucer_window_event_decorated decoratedCallback = (saucer_window _unused, /*saucer_window_decoration*/int val, Callback _unused2) -> {
        if (this.listener != null) {
            SaucerWindowDecoration decoration = SaucerWindowDecoration.LUT[val];
            this.listener.onDecorated(decoration);
        }
    };

    private final saucer_window_event_maximize maximizeCallback = (saucer_window _unused, boolean b, Callback _unused2) -> {
        if (this.listener != null) {
            this.listener.onMaximize(b);
        }
    };

    private final saucer_window_event_minimize minimizeCallback = (saucer_window _unused, boolean b, Callback _unused2) -> {
        if (this.listener != null) {
            this.listener.onMinimize(b);
        }
    };

    private final saucer_window_event_closed closedCallback = (saucer_window _unused, Callback _unused2) -> {
        this.cleanup();
        if (this.listener != null) {
            this.listener.onClosed();
        }
    };

    private final saucer_window_event_resize resizeCallback = (saucer_window _unused, int w, int h, Callback _unused2) -> {
        if (this.listener != null) {
            this.listener.onResize(w, h);
        }
    };

    private final saucer_window_event_focus focusCallback = (saucer_window _unused, boolean b, Callback _unused2) -> {
        if (this.listener != null) {
            this.listener.onFocus(b);
        }
    };

    private final saucer_window_event_close closeRequestCallback = (saucer_window _unused, Callback _unused2) -> {
        if (this.listener != null) {
            if (this.listener.shouldAvoidClosing()) {
                return saucer_policy.BLOCK;
            }
        }
        return saucer_policy.ALLOW;
    };

    /**
     * @deprecated Native interop only.
     * 
     * @implNote   This class does not free() itself automatically, which differs
     *             from most BoxedTypes.
     */
    @Deprecated
    public SaucerWindow(saucer_window window) {
        super(window, false);

        ntv_window.N.saucer_window_on($ref, saucer_window_event.DECORATED, this.decoratedCallback, false, null);
        ntv_window.N.saucer_window_on($ref, saucer_window_event.MAXIMIZE, this.maximizeCallback, false, null);
        ntv_window.N.saucer_window_on($ref, saucer_window_event.MINIMIZE, this.minimizeCallback, false, null);
        ntv_window.N.saucer_window_on($ref, saucer_window_event.CLOSED, this.closedCallback, false, null);
        ntv_window.N.saucer_window_on($ref, saucer_window_event.RESIZE, this.resizeCallback, false, null);
        ntv_window.N.saucer_window_on($ref, saucer_window_event.FOCUS, this.focusCallback, false, null);
        ntv_window.N.saucer_window_on($ref, saucer_window_event.CLOSE, this.closeRequestCallback, false, null);

        instances.add(this);
    }

    public static SaucerWindow create() {
        IntByReference error = new IntByReference();
        saucer_window nativeWindow = ntv_window.N.saucer_window_new(SaucerApp.ntv_app(), error);

        if (error.getValue() != 0) {
            throw new IllegalStateException("Failed to create SaucerWindow, error code: " + error.getValue());
        }

        return new SaucerWindow(nativeWindow);
    }

    public SaucerWebview createWebview() {
        return this.createWebview(null);
    }

    public SaucerWebview createWebview(Consumer<SaucerWebviewOptions> optionsEditor) {
        SaucerWebviewOptions options = new SaucerWebviewOptions($ref);
        if (optionsEditor != null) {
            optionsEditor.accept(options);
        }

        saucer_webview_options $options = SaucerBoxedType.ntv(options);

        IntByReference error = new IntByReference(0);
        saucer_webview webviewNtv = ntv_webview.N.saucer_webview_new($options, error);

        if (error.getValue() != 0) {
            throw new IllegalStateException("Failed to create SaucerWebview (error code " + error.getValue() + ")");
        }

        SaucerWebview[] $wv = new SaucerWebview[1]; // pointer hack
        $wv[0] = new SaucerWebview(webviewNtv, this, () -> {
            this.webviews.remove($wv[0]);
        });

        this.webviews.add($wv[0]);
        return $wv[0];
    }

    private void cleanup() {
        this.isClosed = true;
        instances.remove(this);
        this.asyncExecutor.shutdownNow();
    }

    /**
     * Frees the window and its resources. Additionally, all child webviews will be
     * destroyed as well.
     */
    @JavascriptFunction
    public void destroy() {
        cleanup();
        $ref.close();
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public void dispatchAsync(@NonNull Runnable runnable) {
        this.asyncExecutor.submit(runnable);
    }

    /**
     * @return   whether or not Saucer is visible.
     * 
     * @implNote On some windowing systems, this is inherently unknowable. In that
     *           case, it will match the last action executed (e.g show()/hide())
     *           and <i>may</i> be out of sync with the windowing system.
     * 
     * @see      #show()
     * @see      #hide()
     */
    @JavascriptGetter("isVisible")
    public boolean isVisible() {
        return ntv_window.N.saucer_window_visible($ref);
    }

    /**
     * @return whether or not Saucer is in the foreground/focused.
     */
    @JavascriptGetter("isFocused")
    public boolean isFocused() {
        return ntv_window.N.saucer_window_focused($ref);
    }

    /**
     * Focuses Saucer, bringing it into the foreground.
     */
    @JavascriptFunction
    public void focus() {
        ntv_window.N.saucer_window_focus($ref);
    }

    /**
     * @return whether or not Saucer is minimized.
     */
    @JavascriptGetter("minimized")
    public boolean isMinimized() {
        return ntv_window.N.saucer_window_minimized($ref);
    }

    /**
     * Whether or not to minimize (true) or restore (false)
     */
    @JavascriptSetter("minimized")
    public void setMinimized(boolean b) {
        ntv_window.N.saucer_window_set_minimized($ref, b);
    }

    /**
     * @return whether or not Saucer is maximized.
     */
    @JavascriptGetter("maximized")
    public boolean isMaximized() {
        return ntv_window.N.saucer_window_maximized($ref);
    }

    /**
     * Whether or not to maximize (true) or restore (false)
     */
    @JavascriptSetter("maximized")
    public void setMaximized(boolean b) {
        ntv_window.N.saucer_window_set_maximized($ref, b);
    }

    /**
     * @return whether or not Saucer is resizable by the user.
     */
    @JavascriptGetter("resizable")
    public boolean isResizable() {
        return ntv_window.N.saucer_window_resizable($ref);
    }

    /**
     * Enables (true) or disables (false) the resizing of Saucer.
     */
    @JavascriptSetter("resizable")
    public void setResizable(boolean b) {
        ntv_window.N.saucer_window_set_resizable($ref, b);
    }

    /**
     * @return whether or not Saucer is fullscreen.
     */
    @JavascriptGetter("fullscreen")
    public boolean isFullscreen() {
        return ntv_window.N.saucer_window_fullscreen($ref);
    }

    /**
     * Sets whether or not Saucer is in fullscreen mode.
     */
    @JavascriptSetter("fullscreen")
    public void setFullscreen(boolean b) {
        ntv_window.N.saucer_window_set_fullscreen($ref, b);
    }

    /**
     * @return whether or not Saucer is always on top of every other window.
     */
    @JavascriptGetter("alwaysOnTop")
    public boolean isAlwaysOnTop() {
        return ntv_window.N.saucer_window_always_on_top($ref);
    }

    /**
     * Sets whether or not Saucer is always on top of every other window.
     */
    @JavascriptSetter("alwaysOnTop")
    public void setAlwaysOnTop(boolean b) {
        ntv_window.N.saucer_window_set_always_on_top($ref, b);
    }

    /**
     * @return whether or not Saucer can be clicked through (i.e mouse events pass
     *         through it).
     */
    @JavascriptGetter("clickThrough")
    public boolean isClickThrough() {
        return ntv_window.N.saucer_window_click_through($ref);
    }

    /**
     * Enables (true) or disables (false) click-through for Saucer (i.e mouse events
     * pass through it).
     */
    @JavascriptSetter("clickThrough")
    public void setClickThrough(boolean b) {
        ntv_window.N.saucer_window_set_always_on_top($ref, b);
    }

    /**
     * @return the title of the Saucer window.
     */
    @JavascriptGetter("title")
    public String getTitle() {
        size_t.ByReference sizeRef = new size_t.ByReference();

        // First call to get the size
        ntv_window.N.saucer_window_title($ref, null, sizeRef);

        // Second call to get the actual string
        byte[] buffer = new byte[sizeRef.getValue().intValue()];
        ntv_window.N.saucer_window_title($ref, buffer, sizeRef);

        return new String(buffer, StandardCharsets.UTF_8);
    }

    /**
     * Sets the title of the Saucer window.
     */
    @JavascriptSetter("title")
    public void setTitle(@NonNull String title) {
        ntv_window.N.saucer_window_set_title($ref, title);
    }

    @JavascriptGetter("backgroundColor")
    public SaucerColor backgroundColor() {
        ByteByReference rRef = new ByteByReference();
        ByteByReference gRef = new ByteByReference();
        ByteByReference bRef = new ByteByReference();
        ByteByReference aRef = new ByteByReference();

        ntv_window.N.saucer_window_background($ref, rRef, gRef, bRef, aRef);

        return new SaucerColor(
            Byte.toUnsignedInt(rRef.getValue()),
            Byte.toUnsignedInt(gRef.getValue()),
            Byte.toUnsignedInt(bRef.getValue()),
            Byte.toUnsignedInt(aRef.getValue())
        );
    }

    @JavascriptSetter("backgroundColor")
    public void setBackgroundColor(@NonNull SaucerColor color) {
        ntv_window.N.saucer_window_set_background(
            $ref,
            (byte) color.red,
            (byte) color.green,
            (byte) color.blue,
            (byte) color.alpha
        );
    }

    /**
     * @return whether or not Saucer has decorations (i.e the title bar).
     */
    @JavascriptGetter("decorations")
    public SaucerWindowDecoration getDecorations() {
        int val = ntv_window.N.saucer_window_decorations($ref);
        return SaucerWindowDecoration.LUT[val];
    }

    /**
     * Enables (true) or disables (false) Saucer's window decorations (i.e the title
     * bar).
     */
    @JavascriptSetter("decorations")
    public void setDecorations(SaucerWindowDecoration value) {
        ntv_window.N.saucer_window_set_decorations($ref, value.nativeValue);
    }

    /**
     * @return the size of the Saucer window.
     */
    @JavascriptGetter("size")
    public SaucerSize getSize() {
        IntByReference widthRef = new IntByReference();
        IntByReference heightRef = new IntByReference();

        ntv_window.N.saucer_window_size($ref, widthRef, heightRef);
        return new SaucerSize(widthRef.getValue(), heightRef.getValue());
    }

    /**
     * Sets the size of the Saucer window.
     */
    @JavascriptSetter("size")
    public void setSize(@NonNull SaucerSize size) {
        ntv_window.N.saucer_window_set_size($ref, size.width, size.height);
    }

    /**
     * @return the minimum allowed size of the Saucer window.
     */
    @JavascriptGetter("minSize")
    public SaucerSize getMinSize() {
        IntByReference widthRef = new IntByReference();
        IntByReference heightRef = new IntByReference();

        ntv_window.N.saucer_window_min_size($ref, widthRef, heightRef);
        return new SaucerSize(widthRef.getValue(), heightRef.getValue());
    }

    /**
     * Sets the minimum allowed size of the Saucer window.
     */
    @JavascriptSetter("minSize")
    public void setMinSize(@NonNull SaucerSize size) {
        ntv_window.N.saucer_window_set_min_size($ref, size.width, size.height);
    }

    /**
     * @return the maximum allowed size of the Saucer window.
     */
    @JavascriptGetter("maxSize")
    public SaucerSize getMaxSize() {
        IntByReference widthRef = new IntByReference();
        IntByReference heightRef = new IntByReference();

        ntv_window.N.saucer_window_max_size($ref, widthRef, heightRef);
        return new SaucerSize(widthRef.getValue(), heightRef.getValue());
    }

    /**
     * Sets the maximum allowed size of the Saucer window.
     */
    @JavascriptSetter("maxSize")
    public void setMaxSize(@NonNull SaucerSize size) {
        ntv_window.N.saucer_window_set_max_size($ref, size.width, size.height);
    }

    @JavascriptGetter("position")
    public SaucerPosition getPosition() {
        IntByReference xRef = new IntByReference();
        IntByReference yRef = new IntByReference();

        ntv_window.N.saucer_window_position($ref, xRef, yRef);
        return new SaucerPosition(xRef.getValue(), yRef.getValue());
    }

    @JavascriptSetter("position")
    public void setPosition(@NonNull SaucerPosition position) {
        ntv_window.N.saucer_window_set_position($ref, position.x, position.y);
    }

    @JavascriptGetter("screen")
    public SaucerScreen screen() {
        saucer_screen $screen = ntv_window.N.saucer_window_screen($ref);
        return new SaucerScreen($screen, true);
    }

    /**
     * Hides Saucer, this causes the window to disappear from the taskbar and the
     * user will no longer be able to view the app no matter what they do.
     * 
     * @see #show()
     */
    @JavascriptFunction
    public void hide() {
        ntv_window.N.saucer_window_hide($ref);
    }

    /**
     * Unhides Saucer.
     * 
     * @see #hide()
     */
    @JavascriptFunction
    public void show() {
        ntv_window.N.saucer_window_show($ref);
    }

    /**
     * Changes the application's icon (e.g what you see in the dock/taskbar).
     * 
     * @see {@link SaucerWebview#getFavicon()}
     */
    public void setIcon(@NonNull SaucerIcon icon) {
        ntv_window.N.saucer_window_set_icon($ref, SaucerBoxedType.ntv(icon));
    }

}
