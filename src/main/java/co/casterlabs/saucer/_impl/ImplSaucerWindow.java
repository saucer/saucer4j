package co.casterlabs.saucer._impl;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.SaucerWindow;
import co.casterlabs.saucer._impl.ImplSaucerWindow._Native.WindowBooleanCallback;
import co.casterlabs.saucer._impl.ImplSaucerWindow._Native.WindowCloseEventCallback;
import co.casterlabs.saucer._impl.ImplSaucerWindow._Native.WindowResizeEventCallback;
import co.casterlabs.saucer._impl.ImplSaucerWindow._Native.WindowVoidCallback;
import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.bridge.JavascriptGetter;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.bridge.JavascriptSetter;
import co.casterlabs.saucer.utils.SaucerSize;
import lombok.NonNull;

@JavascriptObject
@SuppressWarnings("deprecation")
class ImplSaucerWindow implements SaucerWindow {
    private static final _Native N = _SaucerNative.load(_Native.class);

    private final ImplSaucer saucer;

    private @Nullable SaucerWindowListener eventListener;

    private WindowBooleanCallback windowEventMaximizeCallback = (Pointer $saucer, boolean isMaximized) -> {
        try {
//            System.out.printf("maximized: %b\n", isMaximized);
            if (this.eventListener == null) return;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WindowBooleanCallback windowEventMinimizeCallback = (Pointer $saucer, boolean isMinimized) -> {
        try {
//            System.out.printf("minimized: %b\n", isMinimized);
            if (this.eventListener == null) return;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WindowVoidCallback windowEventClosedCallback = (Pointer $saucer) -> {
        try {
//            System.out.println("closed");
            if (this.eventListener == null) return;
            this.eventListener.onClosed();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WindowResizeEventCallback windowEventResizeCallback = (Pointer $saucer, int width, int height) -> {
        try {
//            System.out.printf("resize: w=%d, h=%d\n", width, height);
            if (this.eventListener == null) return;
            this.eventListener.onResize(width, height);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WindowBooleanCallback windowEventFocusCallback = (Pointer $saucer, boolean hasFocus) -> {
        try {
//            System.out.printf("focus: %b\n", hasFocus);
            if (this.eventListener == null) return;

            if (hasFocus) {
                this.eventListener.onFocused();
            } else {
                this.eventListener.onBlur();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WindowCloseEventCallback windowEventCloseCallback = (Pointer $saucer) -> {
        try {
//            System.out.println("close");
            if (this.eventListener == null) return false;
            return this.eventListener.shouldAvoidClosing();
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    };

    ImplSaucerWindow(ImplSaucer saucer) {
        this.saucer = saucer;

        N.saucer_window_on(this.saucer.$handle, _Native.SAUCER_WINDOW_EVENT_MAXIMIZE, this.windowEventMaximizeCallback);
        N.saucer_window_on(this.saucer.$handle, _Native.SAUCER_WINDOW_EVENT_MINIMIZE, this.windowEventMinimizeCallback);
        N.saucer_window_on(this.saucer.$handle, _Native.SAUCER_WINDOW_EVENT_CLOSED, this.windowEventClosedCallback);
        N.saucer_window_on(this.saucer.$handle, _Native.SAUCER_WINDOW_EVENT_RESIZE, this.windowEventResizeCallback);
        N.saucer_window_on(this.saucer.$handle, _Native.SAUCER_WINDOW_EVENT_FOCUS, this.windowEventFocusCallback);
        N.saucer_window_on(this.saucer.$handle, _Native.SAUCER_WINDOW_EVENT_CLOSE, this.windowEventCloseCallback);
    }

    @JavascriptGetter("isFocused")
    @Override
    public boolean isFocused() {
        return N.saucer_window_focused(this.saucer.$handle);
    }

    @JavascriptFunction
    @Override
    public void focus() {
        N.saucer_window_focus(this.saucer.$handle);
    }

    @JavascriptGetter("minimized")
    @Override
    public boolean isMinimized() {
        return N.saucer_window_minimized(this.saucer.$handle);
    }

    @JavascriptSetter("minimized")
    @Override
    public void setMinimized(boolean b) {
        N.saucer_window_set_minimized(this.saucer.$handle, b);
    }

    @JavascriptGetter("maximized")
    @Override
    public boolean isMaximized() {
        return N.saucer_window_maximized(this.saucer.$handle);
    }

    @JavascriptSetter("maximized")
    @Override
    public void setMaximized(boolean b) {
        N.saucer_window_set_maximized(this.saucer.$handle, b);
    }

    @JavascriptGetter("resizable")
    @Override
    public boolean isResizable() {
        return N.saucer_window_resizable(this.saucer.$handle);
    }

    @JavascriptSetter("resizable")
    @Override
    public void setResizable(boolean b) {
        N.saucer_window_set_resizable(this.saucer.$handle, b);
    }

    @JavascriptGetter("decorated")
    @Override
    public boolean hasDecorations() {
        return N.saucer_window_decorations(this.saucer.$handle);
    }

    @JavascriptSetter("decorated")
    @Override
    public void showDecorations(boolean b) {
        N.saucer_window_set_decorations(this.saucer.$handle, b);
    }

    @JavascriptGetter("alwaysOnTop")
    @Override
    public boolean isAlwaysOnTop() {
        return N.saucer_window_always_on_top(this.saucer.$handle);
    }

    @JavascriptSetter("alwaysOnTop")
    @Override
    public void setAlwaysOnTop(boolean b) {
        N.saucer_window_set_always_on_top(this.saucer.$handle, b);
    }

    @JavascriptGetter("title")
    @Override
    public String getTitle() {
        return N.saucer_window_title(this.saucer.$handle);
    }

    @JavascriptSetter("title")
    @Override
    public void setTitle(@NonNull String title) {
        N.saucer_window_set_title(this.saucer.$handle, _SafePointer.allocate(title));
    }

    @JavascriptGetter("size")
    @Override
    public SaucerSize getSize() {
        _SafePointer $width = _SafePointer.allocate(Integer.BYTES);
        _SafePointer $height = _SafePointer.allocate(Integer.BYTES);

        N.saucer_window_size(this.saucer.$handle, $width, $height);

        int width = $width.p().getInt(0);
        int height = $height.p().getInt(0);

        return new SaucerSize(width, height);
    }

    @JavascriptSetter("size")
    @Override
    public void setSize(@NonNull SaucerSize size) {
        N.saucer_window_set_size(this.saucer.$handle, size.width, size.height);
    }

    @JavascriptGetter("minSize")
    @Override
    public SaucerSize getMinSize() {
        _SafePointer $width = _SafePointer.allocate(Integer.BYTES);
        _SafePointer $height = _SafePointer.allocate(Integer.BYTES);

        N.saucer_window_min_size(this.saucer.$handle, $width, $height);

        int width = $width.p().getInt(0);
        int height = $height.p().getInt(0);

        return new SaucerSize(width, height);
    }

    @JavascriptSetter("minSize")
    @Override
    public void setMinSize(@NonNull SaucerSize size) {
        N.saucer_window_set_min_size(this.saucer.$handle, size.width, size.height);
    }

    @JavascriptGetter("maxSize")
    @Override
    public SaucerSize getMaxSize() {
        _SafePointer $width = _SafePointer.allocate(Integer.BYTES);
        _SafePointer $height = _SafePointer.allocate(Integer.BYTES);

        N.saucer_window_max_size(this.saucer.$handle, $width, $height);

        int width = $width.p().getInt(0);
        int height = $height.p().getInt(0);

        return new SaucerSize(width, height);
    }

    @JavascriptSetter("maxSize")
    @Override
    public void setMaxSize(@NonNull SaucerSize size) {
        N.saucer_window_set_max_size(this.saucer.$handle, size.width, size.height);
    }

    @JavascriptFunction
    @Override
    public void hide() {
        N.saucer_window_hide(this.saucer.$handle);
    }

    @JavascriptFunction
    @Override
    public void show() {
        N.saucer_window_show(this.saucer.$handle);
    }

    @Override
    public void setListener(@Nullable SaucerWindowListener listener) {
        this.eventListener = listener;
    }

    // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/window.h
    static interface _Native extends Library {
        /** Requires {@link WindowBooleanCallback} */
        static final int SAUCER_WINDOW_EVENT_MAXIMIZE = 0;

        /** Requires {@link WindowBooleanCallback} */
        static final int SAUCER_WINDOW_EVENT_MINIMIZE = 1;

        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WINDOW_EVENT_CLOSED = 2;

        /** Requires {@link WindowResizeEventCallback} */
        static final int SAUCER_WINDOW_EVENT_RESIZE = 3;

        /** Requires {@link WindowBooleanCallback} */
        static final int SAUCER_WINDOW_EVENT_FOCUS = 4;

        /** Requires {@link WindowCloseEventCallback} */
        static final int SAUCER_WINDOW_EVENT_CLOSE = 5;

        boolean saucer_window_focused(_SafePointer $saucer);

        boolean saucer_window_minimized(_SafePointer $saucer);

        boolean saucer_window_maximized(_SafePointer $saucer);

        boolean saucer_window_resizable(_SafePointer $saucer);

        boolean saucer_window_decorations(_SafePointer $saucer);

        boolean saucer_window_always_on_top(_SafePointer $saucer);

        String saucer_window_title(_SafePointer $saucer);

        void saucer_window_size(_SafePointer $saucer, _SafePointer $width, _SafePointer $height);

        void saucer_window_max_size(_SafePointer $saucer, _SafePointer $width, _SafePointer $height);

        void saucer_window_min_size(_SafePointer $saucer, _SafePointer $width, _SafePointer $height);

        void saucer_window_hide(_SafePointer $saucer);

        void saucer_window_show(_SafePointer $saucer);

        void saucer_window_focus(_SafePointer $saucer);

        void saucer_window_set_minimized(_SafePointer $saucer, boolean enabled);

        void saucer_window_set_maximized(_SafePointer $saucer, boolean enabled);

        void saucer_window_set_resizable(_SafePointer $saucer, boolean enabled);

        void saucer_window_set_decorations(_SafePointer $saucer, boolean enabled);

        void saucer_window_set_always_on_top(_SafePointer $saucer, boolean enabled);

        void saucer_window_set_title(_SafePointer $saucer, _SafePointer title);

        void saucer_window_set_size(_SafePointer $saucer, int width, int height);

        void saucer_window_set_max_size(_SafePointer $saucer, int width, int height);

        void saucer_window_set_min_size(_SafePointer $saucer, int width, int height);

        long saucer_window_on(_SafePointer $saucer, int event, Callback callback);

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface WindowResizeEventCallback extends Callback {
            void callback(Pointer $saucer, int width, int height);
        }

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface WindowCloseEventCallback extends Callback {
            boolean callback(Pointer $saucer);
        }

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface WindowBooleanCallback extends Callback {
            void callback(Pointer $saucer, boolean b);
        }

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface WindowVoidCallback extends Callback {
            void callback(Pointer $saucer);
        }

    }

}
