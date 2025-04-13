package app.saucer.webview.window;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import app.saucer.Saucer;
import app.saucer.SaucerApp;
import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptGetter;
import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptSetter;
import app.saucer.ntv._memory;
import app.saucer.ntv._window;
import app.saucer.ntv._window.SAUCER_POLICY;
import app.saucer.ntv._window.SAUCER_WINDOW_EVENT;
import app.saucer.ntv._window.SAUCER_WINDOW_EVENT.WindowCloseRequestCallback;
import app.saucer.ntv._window.SAUCER_WINDOW_EVENT.WindowClosedCallback;
import app.saucer.ntv._window.SAUCER_WINDOW_EVENT.WindowDecoratedCallback;
import app.saucer.ntv._window.SAUCER_WINDOW_EVENT.WindowFocusCallback;
import app.saucer.ntv._window.SAUCER_WINDOW_EVENT.WindowMaxmizeCallback;
import app.saucer.ntv._window.SAUCER_WINDOW_EVENT.WindowMinimizeCallback;
import app.saucer.ntv._window.SAUCER_WINDOW_EVENT.WindowResizeEventCallback;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.SaucerPointerReference;
import app.saucer.ntv.util.size_t;
import app.saucer.util.SaucerSize;
import app.saucer.webview.SaucerWebview;
import lombok.NonNull;
import lombok.Setter;

/**
 * @apiNote This class is not thread-safe. You must call it from the main thread
 *          or use {@link SaucerApp#dispatch(Runnable)} or
 *          {@link SaucerApp#dispatch(Supplier)}.
 */
@SuppressWarnings("deprecation")
@JavascriptObject
public class SaucerWindow {
    private final Saucer saucer;

    private @Setter @Nullable SaucerWindowListener listener;

    private final WindowDecoratedCallback decoratedCallback = (_unused, b) -> {
        if (this.listener != null) {
            this.listener.onDecorated(b);
        }
    };

    private final WindowMaxmizeCallback maximizeCallback = (_unused, b) -> {
        if (this.listener != null) {
            this.listener.onMaximize(b);
        }
    };

    private final WindowMinimizeCallback minimizeCallback = (_unused, b) -> {
        if (this.listener != null) {
            this.listener.onMinimize(b);
        }
    };

    private final WindowClosedCallback closedCallback = (_unused) -> {
        if (this.listener != null) {
            this.listener.onClosed();
        }
    };

    private final WindowResizeEventCallback resizeCallback = (_unused, w, h) -> {
        if (this.listener != null) {
            this.listener.onResize(w, h);
        }
    };

    private final WindowFocusCallback focusCallback = (_unused, b) -> {
        if (this.listener != null) {
            this.listener.onFocus(b);
        }
    };

    private final WindowCloseRequestCallback closeRequestCallback = (_unused) -> {
        if (this.listener != null) {
            if (this.listener.shouldAvoidClosing()) {
                return SAUCER_POLICY.BLOCK;
            }
        }
        return SAUCER_POLICY.ALLOW;
    };

    /**
     * @deprecated Native interop only.
     * 
     * @implNote   This class does not free() itself automatically, which differs
     *             from most BoxedTypes.
     */
    @Deprecated
    public SaucerWindow(Saucer saucer) {
        this.saucer = saucer;

        _window.N.saucer_window_on(saucer.ntv(), SAUCER_WINDOW_EVENT.DECORATED, this.decoratedCallback);
        _window.N.saucer_window_on(saucer.ntv(), SAUCER_WINDOW_EVENT.MAXIMIZE, this.maximizeCallback);
        _window.N.saucer_window_on(saucer.ntv(), SAUCER_WINDOW_EVENT.MINIMIZE, this.minimizeCallback);
        _window.N.saucer_window_on(saucer.ntv(), SAUCER_WINDOW_EVENT.CLOSED, this.closedCallback);
        _window.N.saucer_window_on(saucer.ntv(), SAUCER_WINDOW_EVENT.RESIZE, this.resizeCallback);
        _window.N.saucer_window_on(saucer.ntv(), SAUCER_WINDOW_EVENT.FOCUS, this.focusCallback);
        _window.N.saucer_window_on(saucer.ntv(), SAUCER_WINDOW_EVENT.CLOSE, this.closeRequestCallback);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

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
        return _window.N.saucer_window_visible(this.saucer.ntv());
    }

    /**
     * @return whether or not Saucer is in the foreground/focused.
     */
    @JavascriptGetter("isFocused")
    public boolean isFocused() {
        return _window.N.saucer_window_focused(this.saucer.ntv());
    }

    /**
     * Focuses Saucer, bringing it into the foreground.
     */
    @JavascriptFunction
    public void focus() {
        _window.N.saucer_window_focus(this.saucer.ntv());
    }

    /**
     * @return whether or not Saucer is minimized.
     */
    @JavascriptGetter("minimized")
    public boolean isMinimized() {
        return _window.N.saucer_window_minimized(this.saucer.ntv());
    }

    /**
     * Whether or not to minimize (true) or restore (false)
     */
    @JavascriptSetter("minimized")
    public void setMinimized(boolean b) {
        _window.N.saucer_window_set_minimized(this.saucer.ntv(), b);
    }

    /**
     * @return whether or not Saucer is maximized.
     */
    @JavascriptGetter("maximized")
    public boolean isMaximized() {
        return _window.N.saucer_window_maximized(this.saucer.ntv());
    }

    /**
     * Whether or not to maximize (true) or restore (false)
     */
    @JavascriptSetter("maximized")
    public void setMaximized(boolean b) {
        _window.N.saucer_window_set_maximized(this.saucer.ntv(), b);
    }

    /**
     * @return whether or not Saucer is resizable by the user.
     */
    @JavascriptGetter("resizable")
    public boolean isResizable() {
        return _window.N.saucer_window_resizable(this.saucer.ntv());
    }

    /**
     * Enables (true) or disables (false) the resizing of Saucer.
     */
    @JavascriptSetter("resizable")
    public void setResizable(boolean b) {
        _window.N.saucer_window_set_resizable(this.saucer.ntv(), b);
    }

    /**
     * @return whether or not Saucer has decorations (i.e the title bar).
     */
    @JavascriptGetter("decorations")
    public boolean hasDecorations() {
        return _window.N.saucer_window_decorations(this.saucer.ntv());
    }

    /**
     * Enables (true) or disables (false) Saucer's window decorations (i.e the title
     * bar).
     */
    @JavascriptSetter("decorations")
    public void showDecorations(boolean b) {
        _window.N.saucer_window_set_decorations(this.saucer.ntv(), b);
    }

    /**
     * @return whether or not Saucer is always on top of every other window.
     */
    @JavascriptGetter("alwaysOnTop")
    public boolean isAlwaysOnTop() {
        return _window.N.saucer_window_always_on_top(this.saucer.ntv());
    }

    /**
     * Sets whether or not Saucer is always on top of every other window.
     */
    @JavascriptSetter("alwaysOnTop")
    public void setAlwaysOnTop(boolean b) {
        _window.N.saucer_window_set_always_on_top(this.saucer.ntv(), b);
    }

    /**
     * @return the title of the Saucer window.
     */
    @JavascriptGetter("title")
    public String getTitle() {
        try (SaucerPointerReference<String> titleRef = _window.N.saucer_window_title(this.saucer.ntv())) {
            return titleRef.asString();
        }
    }

    /**
     * Sets the title of the Saucer window.
     */
    @JavascriptSetter("title")
    public void setTitle(@NonNull String title) {
        _window.N.saucer_window_set_title(this.saucer.ntv(), title);
    }

    /**
     * @return the size of the Saucer window.
     */
    @JavascriptGetter("size")
    public SaucerSize getSize() {
        try (
            SaucerPointerReference<Integer> widthRef = _memory.N.saucer_memory_alloc(new size_t(Integer.BYTES));
            SaucerPointerReference<Integer> heightRef = _memory.N.saucer_memory_alloc(new size_t(Integer.BYTES))) {
            _window.N.saucer_window_size(this.saucer.ntv(), widthRef, heightRef);
            return new SaucerSize(widthRef.asInt(), heightRef.asInt());
        }
    }

    /**
     * Sets the size of the Saucer window.
     */
    @JavascriptSetter("size")
    public void setSize(@NonNull SaucerSize size) {
        _window.N.saucer_window_set_size(this.saucer.ntv(), size.width, size.height);
    }

    /**
     * @return the minimum allowed size of the Saucer window.
     */
    @JavascriptGetter("minSize")
    public SaucerSize getMinSize() {
        try (
            SaucerPointerReference<Integer> widthRef = _memory.N.saucer_memory_alloc(new size_t(Integer.BYTES));
            SaucerPointerReference<Integer> heightRef = _memory.N.saucer_memory_alloc(new size_t(Integer.BYTES))) {
            _window.N.saucer_window_min_size(this.saucer.ntv(), widthRef, heightRef);
            return new SaucerSize(widthRef.asInt(), heightRef.asInt());
        }
    }

    /**
     * Sets the minimum allowed size of the Saucer window.
     */
    @JavascriptSetter("minSize")
    public void setMinSize(@NonNull SaucerSize size) {
        _window.N.saucer_window_set_min_size(this.saucer.ntv(), size.width, size.height);
    }

    /**
     * @return the maximum allowed size of the Saucer window.
     */
    @JavascriptGetter("maxSize")
    public SaucerSize getMaxSize() {
        try (
            SaucerPointerReference<Integer> widthRef = _memory.N.saucer_memory_alloc(new size_t(Integer.BYTES));
            SaucerPointerReference<Integer> heightRef = _memory.N.saucer_memory_alloc(new size_t(Integer.BYTES))) {
            _window.N.saucer_window_max_size(this.saucer.ntv(), widthRef, heightRef);
            return new SaucerSize(widthRef.asInt(), heightRef.asInt());
        }
    }

    /**
     * Sets the maximum allowed size of the Saucer window.
     */
    @JavascriptSetter("maxSize")
    public void setMaxSize(@NonNull SaucerSize size) {
        _window.N.saucer_window_set_max_size(this.saucer.ntv(), size.width, size.height);
    }

    /**
     * Hides Saucer, this causes the window to disappear from the taskbar and the
     * user will no longer be able to view the app no matter what they do.
     * 
     * @see #show()
     */
    @JavascriptFunction
    public void hide() {
        _window.N.saucer_window_hide(this.saucer.ntv());
    }

    /**
     * Unhides Saucer.
     * 
     * @see #hide()
     */
    @JavascriptFunction
    public void show() {
        _window.N.saucer_window_show(this.saucer.ntv());
    }

    /**
     * Changes the application's icon (e.g what you see in the dock/taskbar).
     * 
     * @see {@link SaucerWebview#getFavicon()}
     */
    public void setIcon(@NonNull SaucerIcon icon) {
        _window.N.saucer_window_set_icon(this.saucer.ntv(), SaucerBoxedType.ntv(icon));
    }

}
