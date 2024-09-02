package co.casterlabs.saucer._impl;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.SaucerWindow;
import co.casterlabs.saucer._impl.ImplSaucerWindow._Native.SAUCER_WINDOW_EVENT;
import co.casterlabs.saucer._impl.ImplSaucerWindow._Native.WindowBooleanCallback;
import co.casterlabs.saucer._impl.ImplSaucerWindow._Native.WindowCloseEventCallback;
import co.casterlabs.saucer._impl.ImplSaucerWindow._Native.WindowResizeEventCallback;
import co.casterlabs.saucer._impl.ImplSaucerWindow._Native.WindowVoidCallback;
import co.casterlabs.saucer._impl.c._SaucerMemory;
import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.bridge.JavascriptGetter;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.bridge.JavascriptSetter;
import co.casterlabs.saucer.utils.SaucerIcon;
import co.casterlabs.saucer.utils.SaucerSize;
import lombok.NonNull;
import lombok.SneakyThrows;

@JavascriptObject
@SuppressWarnings("deprecation")
class ImplSaucerWindow implements SaucerWindow {
    private static final _Native N = _SaucerNative.load(_Native.class);

    private final _ImplSaucer saucer;

    private @Nullable SaucerWindowListener eventListener;

    private WindowBooleanCallback windowEventDecoratedCallback = (Pointer $saucer, boolean isDecorated) -> {
        try {
//            System.out.printf("maximized: %b\n", isMaximized);
            if (this.eventListener == null) return;
            this.eventListener.onDecorated(isDecorated);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WindowBooleanCallback windowEventMaximizeCallback = (Pointer $saucer, boolean isMaximized) -> {
        try {
//            System.out.printf("maximized: %b\n", isMaximized);
            if (this.eventListener == null) return;
            this.eventListener.onMaximize(isMaximized);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private WindowBooleanCallback windowEventMinimizeCallback = (Pointer $saucer, boolean isMinimized) -> {
        try {
//            System.out.printf("minimized: %b\n", isMinimized);
            if (this.eventListener == null) return;
            this.eventListener.onMinimize(isMinimized);
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

            this.eventListener.onFocus(hasFocus);
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

    ImplSaucerWindow(_ImplSaucer saucer) {
        this.saucer = saucer;

        N.saucer_window_on(this.saucer, SAUCER_WINDOW_EVENT.DECORATED.ordinal(), this.windowEventDecoratedCallback);
        N.saucer_window_on(this.saucer, SAUCER_WINDOW_EVENT.MAXIMIZE.ordinal(), this.windowEventMaximizeCallback);
        N.saucer_window_on(this.saucer, SAUCER_WINDOW_EVENT.MINIMIZE.ordinal(), this.windowEventMinimizeCallback);
        N.saucer_window_on(this.saucer, SAUCER_WINDOW_EVENT.CLOSED.ordinal(), this.windowEventClosedCallback);
        N.saucer_window_on(this.saucer, SAUCER_WINDOW_EVENT.RESIZE.ordinal(), this.windowEventResizeCallback);
        N.saucer_window_on(this.saucer, SAUCER_WINDOW_EVENT.FOCUS.ordinal(), this.windowEventFocusCallback);
        N.saucer_window_on(this.saucer, SAUCER_WINDOW_EVENT.CLOSE.ordinal(), this.windowEventCloseCallback);
    }

    @Override
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public <T> T dispatch(@NonNull Supplier<T> task) {
        // Pointers to avoid Java's final requirements.
        // A CompletableFuture would be cleaner, but let's avoid creating garbage.
        Object[] $result = new Object[1];
        Throwable[] $exception = new Throwable[1];

        N.saucer_window_dispatch(this.saucer, () -> {
            try {
                $result[0] = task.get();
            } catch (Throwable t) {
                $exception[0] = t;
            }
        });

        if ($exception[0] == null) {
            return (T) $result[0];
        } else {
            throw $exception[0];
        }
    }

    @JavascriptGetter("isFocused")
    @Override
    public boolean isFocused() {
        return N.saucer_window_focused(this.saucer);
    }

    @JavascriptFunction
    @Override
    public void focus() {
        N.saucer_window_focus(this.saucer);
    }

    @JavascriptGetter("minimized")
    @Override
    public boolean isMinimized() {
        return N.saucer_window_minimized(this.saucer);
    }

    @JavascriptSetter("minimized")
    @Override
    public void setMinimized(boolean b) {
        N.saucer_window_set_minimized(this.saucer, b);
    }

    @JavascriptGetter("maximized")
    @Override
    public boolean isMaximized() {
        return N.saucer_window_maximized(this.saucer);
    }

    @JavascriptSetter("maximized")
    @Override
    public void setMaximized(boolean b) {
        N.saucer_window_set_maximized(this.saucer, b);
    }

    @JavascriptGetter("resizable")
    @Override
    public boolean isResizable() {
        return N.saucer_window_resizable(this.saucer);
    }

    @JavascriptSetter("resizable")
    @Override
    public void setResizable(boolean b) {
        N.saucer_window_set_resizable(this.saucer, b);
    }

    @JavascriptGetter("decorated")
    @Override
    public boolean hasDecorations() {
        return N.saucer_window_decorations(this.saucer);
    }

    @JavascriptSetter("decorated")
    @Override
    public void showDecorations(boolean b) {
        N.saucer_window_set_decorations(this.saucer, b);
    }

    @JavascriptGetter("alwaysOnTop")
    @Override
    public boolean isAlwaysOnTop() {
        return N.saucer_window_always_on_top(this.saucer);
    }

    @JavascriptSetter("alwaysOnTop")
    @Override
    public void setAlwaysOnTop(boolean b) {
        N.saucer_window_set_always_on_top(this.saucer, b);
    }

    @JavascriptGetter("title")
    @Override
    public String getTitle() {
        return N.saucer_window_title(this.saucer);
    }

    @JavascriptSetter("title")
    @Override
    public void setTitle(@NonNull String title) {
        N.saucer_window_set_title(this.saucer, title);
    }

    @JavascriptGetter("size")
    @Override
    public SaucerSize getSize() {
        Pointer $width = _SaucerMemory.alloc(Integer.BYTES);
        Pointer $height = _SaucerMemory.alloc(Integer.BYTES);

        N.saucer_window_size(this.saucer, $width, $height);

        int width = $width.getInt(0);
        int height = $height.getInt(0);

        _SaucerMemory.free($width);
        _SaucerMemory.free($height);

        return new SaucerSize(width, height);
    }

    @JavascriptSetter("size")
    @Override
    public void setSize(@NonNull SaucerSize size) {
        N.saucer_window_set_size(this.saucer, size.width, size.height);
    }

    @JavascriptGetter("minSize")
    @Override
    public SaucerSize getMinSize() {
        Pointer $width = _SaucerMemory.alloc(Integer.BYTES);
        Pointer $height = _SaucerMemory.alloc(Integer.BYTES);

        N.saucer_window_min_size(this.saucer, $width, $height);

        int width = $width.getInt(0);
        int height = $height.getInt(0);

        _SaucerMemory.free($width);
        _SaucerMemory.free($height);

        return new SaucerSize(width, height);
    }

    @JavascriptSetter("minSize")
    @Override
    public void setMinSize(@NonNull SaucerSize size) {
        N.saucer_window_set_min_size(this.saucer, size.width, size.height);
    }

    @JavascriptGetter("maxSize")
    @Override
    public SaucerSize getMaxSize() {
        Pointer $width = _SaucerMemory.alloc(Integer.BYTES);
        Pointer $height = _SaucerMemory.alloc(Integer.BYTES);

        N.saucer_window_max_size(this.saucer, $width, $height);

        int width = $width.getInt(0);
        int height = $height.getInt(0);

        _SaucerMemory.free($width);
        _SaucerMemory.free($height);

        return new SaucerSize(width, height);
    }

    @JavascriptSetter("maxSize")
    @Override
    public void setMaxSize(@NonNull SaucerSize size) {
        N.saucer_window_set_max_size(this.saucer, size.width, size.height);
    }

    @JavascriptFunction
    @Override
    public void hide() {
        N.saucer_window_hide(this.saucer);
    }

    @JavascriptFunction
    @Override
    public void show() {
        N.saucer_window_show(this.saucer);
    }

    @Override
    public void setIcon(@NonNull SaucerIcon icon) {
        N.saucer_window_set_icon(this.saucer, icon);
    }

    @Override
    public void setListener(@Nullable SaucerWindowListener listener) {
        this.eventListener = listener;
    }

    // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/window.h
    static interface _Native extends Library {
        static enum SAUCER_WINDOW_EVENT {
            /** Requires {@link WindowBooleanCallback} */
            DECORATED,

            /** Requires {@link WindowBooleanCallback} */
            MAXIMIZE,

            /** Requires {@link WindowBooleanCallback} */
            MINIMIZE,

            /** Requires {@link WindowVoidCallback} */
            CLOSED,

            /** Requires {@link WindowResizeEventCallback} */
            RESIZE,

            /** Requires {@link WindowBooleanCallback} */
            FOCUS,

            /** Requires {@link WindowCloseEventCallback} */
            CLOSE,
        }

        void saucer_window_hide(_ImplSaucer saucer);

        void saucer_window_show(_ImplSaucer saucer);

        boolean saucer_window_focused(_ImplSaucer saucer);

        void saucer_window_focus(_ImplSaucer saucer);

        boolean saucer_window_minimized(_ImplSaucer saucer);

        void saucer_window_set_minimized(_ImplSaucer saucer, boolean enabled);

        boolean saucer_window_maximized(_ImplSaucer saucer);

        void saucer_window_set_maximized(_ImplSaucer saucer, boolean enabled);

        boolean saucer_window_resizable(_ImplSaucer saucer);

        void saucer_window_set_resizable(_ImplSaucer saucer, boolean enabled);

        boolean saucer_window_decorations(_ImplSaucer saucer);

        void saucer_window_set_decorations(_ImplSaucer saucer, boolean enabled);

        boolean saucer_window_always_on_top(_ImplSaucer saucer);

        void saucer_window_set_always_on_top(_ImplSaucer saucer, boolean enabled);

        void saucer_window_set_title(_ImplSaucer saucer, String title);

        String saucer_window_title(_ImplSaucer saucer);

        void saucer_window_size(_ImplSaucer saucer, Pointer $width, Pointer $height);

        void saucer_window_set_size(_ImplSaucer saucer, int width, int height);

        void saucer_window_min_size(_ImplSaucer saucer, Pointer $width, Pointer $height);

        void saucer_window_set_min_size(_ImplSaucer saucer, int width, int height);

        void saucer_window_max_size(_ImplSaucer saucer, Pointer $width, Pointer $height);

        void saucer_window_set_max_size(_ImplSaucer saucer, int width, int height);

        long saucer_window_on(_ImplSaucer saucer, int event, Callback callback);

        void saucer_window_dispatch(_ImplSaucer saucer, DispatchCallback callback);

        void saucer_window_set_icon(_ImplSaucer saucer, SaucerIcon icon);

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

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface DispatchCallback extends Callback {
            void callback();
        }

    }

}
