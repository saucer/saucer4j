package co.casterlabs.saucer.natives;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.SaucerWindow;
import co.casterlabs.saucer.documentation.PointerType;
import co.casterlabs.saucer.natives.ImplSaucerWindow._Native.BooleanCallback;
import co.casterlabs.saucer.natives.ImplSaucerWindow._Native.CloseEventCallback;
import co.casterlabs.saucer.natives.ImplSaucerWindow._Native.ResizeEventCallback;
import co.casterlabs.saucer.utils.SaucerColor;
import co.casterlabs.saucer.utils.SaucerSize;
import lombok.NonNull;

@SuppressWarnings("deprecation")
class ImplSaucerWindow implements SaucerWindow {
    private static final _Native N = _SaucerNative.load(_Native.class);

    private final @PointerType ImplSaucer $saucer;

    private @Nullable SaucerWindowListener eventListener;

    private BooleanCallback windowEventMaximizeCallback = (boolean isMaximized) -> {
        try {
            if (this.eventListener == null) return;
            System.out.printf("maximized: %b\n", isMaximized);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };
    private BooleanCallback windowEventMinimizeCallback = (boolean isMinimized) -> {
        try {
            if (this.eventListener == null) return;
            System.out.printf("minimized: %b\n", isMinimized);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private VoidCallback windowEventClosedCallback = () -> {
        try {
            if (this.eventListener == null) return;
            this.eventListener.onClosed();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private ResizeEventCallback windowEventResizeCallback = (int width, int height) -> {
        try {
            if (this.eventListener == null) return;
            this.eventListener.onResize(width, height);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private BooleanCallback windowEventFocusCallback = (boolean hasFocus) -> {
        try {
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

    private CloseEventCallback windowEventCloseCallback = () -> {
        try {
            if (this.eventListener == null) return false;
            return this.eventListener.shouldAvoidClosing();
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    };

    ImplSaucerWindow(ImplSaucer $saucer) {
        this.$saucer = $saucer;

        // TODO broken.
//        N.saucer_window_on($saucer.p(), _Native.SAUCER_WINDOW_EVENT_MAXIMIZE, this.windowEventMaximizeCallback);
//        N.saucer_window_on($saucer.p(), _Native.SAUCER_WINDOW_EVENT_MINIMIZE, this.windowEventMinimizeCallback);
//        N.saucer_window_on($saucer.p(), _Native.SAUCER_WINDOW_EVENT_CLOSED, this.windowEventClosedCallback);
//        N.saucer_window_on($saucer.p(), _Native.SAUCER_WINDOW_EVENT_RESIZE, this.windowEventResizeCallback);
//        N.saucer_window_on($saucer.p(), _Native.SAUCER_WINDOW_EVENT_FOCUS, this.windowEventFocusCallback);
//        N.saucer_window_on($saucer.p(), _Native.SAUCER_WINDOW_EVENT_CLOSE, this.windowEventCloseCallback);
    }

    @Override
    public boolean isFocused() {
        return N.saucer_window_focused($saucer.p());
    }

    @Override
    public void focus() {
        N.saucer_window_focus($saucer.p());
    }

    @Override
    public boolean isMinimized() {
        return N.saucer_window_minimized($saucer.p());
    }

    @Override
    public void setMinimized(boolean b) {
        N.saucer_window_set_minimized($saucer.p(), b);
    }

    @Override
    public boolean isMaximized() {
        return N.saucer_window_maximized($saucer.p());
    }

    @Override
    public void setMaximized(boolean b) {
        N.saucer_window_set_maximized($saucer.p(), b);
    }

    @Override
    public boolean isResizable() {
        return N.saucer_window_resizable($saucer.p());
    }

    @Override
    public void setResizable(boolean b) {
        N.saucer_window_set_resizable($saucer.p(), b);
    }

    @Override
    public boolean hasDecorations() {
        return N.saucer_window_decorations($saucer.p());
    }

    @Override
    public void showDecorations(boolean b) {
        N.saucer_window_set_decorations($saucer.p(), b);
    }

    @Override
    public boolean isAlwaysOnTop() {
        return N.saucer_window_always_on_top($saucer.p());
    }

    @Override
    public void setAlwaysOnTop(boolean b) {
        N.saucer_window_set_always_on_top($saucer.p(), b);
    }

    @Override
    public SaucerColor getBackground() {
        Pointer $background = N.saucer_window_background($saucer.p());
        return new SaucerColor($background);
    }

    @Override
    public void setBackground(@NonNull SaucerColor color) {
        N.saucer_window_set_background($saucer.p(), color.p());
    }

    @Override
    public String getTitle() {
        return N.saucer_window_title($saucer.p());
    }

    @Override
    public void setTitle(@NonNull String title) {
        N.saucer_window_set_title($saucer.p(), title);
    }

    @Override
    public SaucerSize getSize() {
        _SafePointer $width = _SafePointer.allocate(Integer.BYTES);
        _SafePointer $height = _SafePointer.allocate(Integer.BYTES);

        N.saucer_window_size($saucer.p(), $width.p(), $height.p());

        int width = $width.p().getInt(0);
        int height = $height.p().getInt(0);

        return new SaucerSize(width, height);
    }

    @Override
    public void setSize(@NonNull SaucerSize size) {
        N.saucer_window_set_size($saucer.p(), size.width, size.height);
    }

    @Override
    public SaucerSize getMinSize() {
        _SafePointer $width = _SafePointer.allocate(Integer.BYTES);
        _SafePointer $height = _SafePointer.allocate(Integer.BYTES);

        N.saucer_window_min_size($saucer.p(), $width.p(), $height.p());

        int width = $width.p().getInt(0);
        int height = $height.p().getInt(0);

        return new SaucerSize(width, height);
    }

    @Override
    public void setMinSize(@NonNull SaucerSize size) {
        N.saucer_window_set_min_size($saucer.p(), size.width, size.height);
    }

    @Override
    public SaucerSize getMaxSize() {
        _SafePointer $width = _SafePointer.allocate(Integer.BYTES);
        _SafePointer $height = _SafePointer.allocate(Integer.BYTES);

        N.saucer_window_max_size($saucer.p(), $width.p(), $height.p());

        int width = $width.p().getInt(0);
        int height = $height.p().getInt(0);

        return new SaucerSize(width, height);
    }

    @Override
    public void setMaxSize(@NonNull SaucerSize size) {
        N.saucer_window_set_max_size($saucer.p(), size.width, size.height);
    }

    @Override
    public void hide() {
        N.saucer_window_hide($saucer.p());
    }

    @Override
    public void show() {
        N.saucer_window_show($saucer.p());
    }

    @Override
    public void setListener(@Nullable SaucerWindowListener listener) {
        this.eventListener = listener;
    }

    // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/window.h
    static interface _Native extends Library {
        /** Requires {@link BooleanCallback} */
        static final int SAUCER_WINDOW_EVENT_MAXIMIZE = 0;

        /** Requires {@link BooleanCallback} */
        static final int SAUCER_WINDOW_EVENT_MINIMIZE = 1;

        /** Requires {@link VoidCallback} */
        static final int SAUCER_WINDOW_EVENT_CLOSED = 2;

        /** Requires {@link ResizeEventCallback} */
        static final int SAUCER_WINDOW_EVENT_RESIZE = 3;

        /** Requires {@link BooleanCallback} */
        static final int SAUCER_WINDOW_EVENT_FOCUS = 4;

        /** Requires {@link CloseEventCallback} */
        static final int SAUCER_WINDOW_EVENT_CLOSE = 5;

        boolean saucer_window_focused(Pointer $saucer);

        boolean saucer_window_minimized(Pointer $saucer);

        boolean saucer_window_maximized(Pointer $saucer);

        boolean saucer_window_resizable(Pointer $saucer);

        boolean saucer_window_decorations(Pointer $saucer);

        boolean saucer_window_always_on_top(Pointer $saucer);

        Pointer saucer_window_background(Pointer $saucer);

        String saucer_window_title(Pointer $saucer);

        void saucer_window_size(Pointer $saucer, Pointer $width, Pointer $height);

        void saucer_window_max_size(Pointer $saucer, Pointer $width, Pointer $height);

        void saucer_window_min_size(Pointer $saucer, Pointer $width, Pointer $height);

        void saucer_window_hide(Pointer $saucer);

        void saucer_window_show(Pointer $saucer);

        void saucer_window_focus(Pointer $saucer);

        void saucer_window_set_minimized(Pointer $saucer, boolean enabled);

        void saucer_window_set_maximized(Pointer $saucer, boolean enabled);

        void saucer_window_set_resizable(Pointer $saucer, boolean enabled);

        void saucer_window_set_decorations(Pointer $saucer, boolean enabled);

        void saucer_window_set_always_on_top(Pointer $saucer, boolean enabled);

        void saucer_window_set_title(Pointer $saucer, String title);

        void saucer_window_set_background(Pointer $saucer, Pointer $color);

        void saucer_window_set_size(Pointer $saucer, int width, int height);

        void saucer_window_set_max_size(Pointer $saucer, int width, int height);

        void saucer_window_set_min_size(Pointer $saucer, int width, int height);

        long saucer_window_on(Pointer $saucer, int event, Callback callback);

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface ResizeEventCallback extends Callback {
            void callback(int width, int height);
        }

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface CloseEventCallback extends Callback {
            boolean callback();
        }

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface BooleanCallback extends Callback {
            void callback(boolean b);
        }

    }

}
