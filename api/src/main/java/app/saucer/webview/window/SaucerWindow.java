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
import app.saucer.util.SaucerScreen;
import app.saucer.util.SaucerSize;
import app.saucer.webview.SaucerWebview;
import app.saucer.webview.SaucerWebviewOptions;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @apiNote This class is not thread-safe. You must call it from the main thread
 *          or use {@link SaucerApp#dispatch(Runnable)} or
 *          {@link SaucerApp#dispatch(Supplier)}.
 */
@JavascriptObject
@SuppressWarnings("deprecation")
@Accessors(fluent = true, chain = true)
public final class SaucerWindow extends SaucerBoxedType<saucer_window> {
    private static final Set<SaucerWindow> instances = new HashSet<>();

    private final ExecutorService asyncExecutor;
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

        this.asyncExecutor = Executors.newCachedThreadPool((r) -> {
            Thread t = new Thread(r);
            t.setName("SaucerWindow - AsyncExecutor #" + t.getId());
            return t;
        });
    }

    /**
     * Creates a new SaucerWindow.
     */
    public static SaucerWindow create() {
        IntByReference error = new IntByReference();
        saucer_window nativeWindow = ntv_window.N.saucer_window_new(SaucerApp.ntv_app(), error);

        if (error.getValue() != 0) {
            throw new IllegalStateException("Failed to create SaucerWindow, error code: " + error.getValue());
        }

        return new SaucerWindow(nativeWindow);
    }

    /**
     * Creates a new SaucerWebview as a child of this SaucerWindow with default
     * options.
     * 
     * @return the created SaucerWebview.
     */
    public SaucerWebview createWebview() {
        return this.createWebview(null);
    }

    /**
     * Creates a new SaucerWebview as a child of this SaucerWindow with custom
     * options.
     * 
     * @param  optionsEditor a consumer that edits the default options.
     * 
     * @return               the created SaucerWebview.
     */
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
    @JavascriptFunction(ignoreReturn = true)
    public void destroy() {
        cleanup();
        $ref.close();
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * Dispatches a runnable to be executed asynchronously on a separate thread.
     * 
     * @param runnable the runnable to execute.
     */
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
     * 
     * @return this instance, for chaining.
     */
    @JavascriptFunction(ignoreReturn = true)
    public SaucerWindow focus() {
        ntv_window.N.saucer_window_focus($ref);
        return this;
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
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("minimized")
    public SaucerWindow minimized(boolean b) {
        ntv_window.N.saucer_window_set_minimized($ref, b);
        return this;
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
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("maximized")
    public SaucerWindow maximized(boolean b) {
        ntv_window.N.saucer_window_set_maximized($ref, b);
        return this;
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
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("resizable")
    public SaucerWindow resizable(boolean b) {
        ntv_window.N.saucer_window_set_resizable($ref, b);
        return this;
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
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("fullscreen")
    public SaucerWindow fullscreen(boolean b) {
        ntv_window.N.saucer_window_set_fullscreen($ref, b);
        return this;
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
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("alwaysOnTop")
    public SaucerWindow alwaysOnTop(boolean b) {
        ntv_window.N.saucer_window_set_always_on_top($ref, b);
        return this;
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
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("clickThrough")
    public SaucerWindow clickThrough(boolean b) {
        ntv_window.N.saucer_window_set_always_on_top($ref, b);
        return this;
    }

    /**
     * @return the title of the Saucer window.
     */
    @JavascriptGetter("title")
    public String title() {
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
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("title")
    public SaucerWindow title(@NonNull String title) {
        ntv_window.N.saucer_window_set_title($ref, title);
        return this;
    }

    /**
     * @return the background color of the Saucer window.
     */
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

    /**
     * Sets the background color of the Saucer window.
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("backgroundColor")
    public SaucerWindow backgroundColor(@NonNull SaucerColor color) {
        ntv_window.N.saucer_window_set_background(
            $ref,
            (byte) color.red,
            (byte) color.green,
            (byte) color.blue,
            (byte) color.alpha
        );
        return this;
    }

    /**
     * @return whether or not Saucer has decorations (i.e the title bar).
     */
    @JavascriptGetter("decorations")
    public SaucerWindowDecoration decorations() {
        int val = ntv_window.N.saucer_window_decorations($ref);
        return SaucerWindowDecoration.LUT[val];
    }

    /**
     * Enables (true) or disables (false) Saucer's window decorations (i.e the title
     * bar).
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("decorations")
    public SaucerWindow decorations(@NonNull SaucerWindowDecoration value) {
        ntv_window.N.saucer_window_set_decorations($ref, value.nativeValue);
        return this;
    }

    /**
     * @return the size of the Saucer window.
     */
    @JavascriptGetter("size")
    public SaucerSize size() {
        IntByReference widthRef = new IntByReference();
        IntByReference heightRef = new IntByReference();

        ntv_window.N.saucer_window_size($ref, widthRef, heightRef);
        return new SaucerSize(widthRef.getValue(), heightRef.getValue());
    }

    /**
     * Sets the size of the Saucer window.
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("size")
    public SaucerWindow size(@NonNull SaucerSize size) {
        ntv_window.N.saucer_window_set_size($ref, size.width, size.height);
        return this;
    }

    /**
     * @return the minimum allowed size of the Saucer window.
     */
    @JavascriptGetter("minSize")
    public SaucerSize minSize() {
        IntByReference widthRef = new IntByReference();
        IntByReference heightRef = new IntByReference();

        ntv_window.N.saucer_window_min_size($ref, widthRef, heightRef);
        return new SaucerSize(widthRef.getValue(), heightRef.getValue());
    }

    /**
     * Sets the minimum allowed size of the Saucer window.
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("minSize")
    public SaucerWindow minSize(@NonNull SaucerSize size) {
        ntv_window.N.saucer_window_set_min_size($ref, size.width, size.height);
        return this;
    }

    /**
     * @return the maximum allowed size of the Saucer window.
     */
    @JavascriptGetter("maxSize")
    public SaucerSize maxSize() {
        IntByReference widthRef = new IntByReference();
        IntByReference heightRef = new IntByReference();

        ntv_window.N.saucer_window_max_size($ref, widthRef, heightRef);
        return new SaucerSize(widthRef.getValue(), heightRef.getValue());
    }

    /**
     * Sets the maximum allowed size of the Saucer window.
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("maxSize")
    public SaucerWindow maxSize(@NonNull SaucerSize size) {
        ntv_window.N.saucer_window_set_max_size($ref, size.width, size.height);
        return this;
    }

    /**
     * @return the position of the Saucer window.
     */
    @JavascriptGetter("position")
    public SaucerPosition position() {
        IntByReference xRef = new IntByReference();
        IntByReference yRef = new IntByReference();

        ntv_window.N.saucer_window_position($ref, xRef, yRef);
        return new SaucerPosition(xRef.getValue(), yRef.getValue());
    }

    /**
     * Sets the position of the Saucer window.
     * 
     * @return this instance, for chaining.
     */
    @JavascriptSetter("position")
    public SaucerWindow position(@NonNull SaucerPosition position) {
        ntv_window.N.saucer_window_set_position($ref, position.x, position.y);
        return this;
    }

    /**
     * @return the screen that Saucer is currently on.
     */
    @JavascriptGetter("screen")
    public SaucerScreen screen() {
        saucer_screen $screen = ntv_window.N.saucer_window_screen($ref);
        return new SaucerScreen($screen, true);
    }

    /**
     * Hides Saucer, this causes the window to disappear from the taskbar and the
     * user will no longer be able to view the app no matter what they do.
     * 
     * @return this instance, for chaining.
     * 
     * @see    #show()
     */
    @JavascriptFunction(ignoreReturn = true)
    public SaucerWindow hide() {
        ntv_window.N.saucer_window_hide($ref);
        return this;
    }

    /**
     * Unhides Saucer.
     * 
     * @return this instance, for chaining.
     * 
     * @see    #hide()
     */
    @JavascriptFunction(ignoreReturn = true)
    public SaucerWindow show() {
        ntv_window.N.saucer_window_show($ref);
        return this;
    }

    /**
     * Changes the application's icon (e.g what you see in the dock/taskbar).
     * 
     * @return this instance, for chaining.
     * 
     * @see    {@link SaucerWebview#getFavicon()}
     */
    public SaucerWindow icon(@NonNull SaucerIcon icon) {
        ntv_window.N.saucer_window_set_icon($ref, SaucerBoxedType.ntv(icon));
        return this;
    }

}
