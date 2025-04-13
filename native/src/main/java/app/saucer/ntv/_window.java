package app.saucer.ntv;

import com.sun.jna.Callback;
import com.sun.jna.Library;

import app.saucer.ntv._icon.saucer_icon;
import app.saucer.ntv.documentation.NoInline;
import app.saucer.ntv.documentation.RequiresFree;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerReference;
import app.saucer.ntv.util.SaucerPointerType;

public interface _window extends Library {
    public static final _window N = SaucerNativeLoader.load(_window.class);

    public static class SAUCER_WINDOW_EVENT {
        /** Requires {@link WindowDecoratedCallback} */
        public static final int DECORATED = 0;

        @NoInline
        public static interface WindowDecoratedCallback extends Callback {
            void callback(saucer_handle saucer, boolean b);
        }

        /* ------------------------ */
        /* ------------------------ */
        /* ------------------------ */

        /** Requires {@link WindowMaxmizeCallback} */
        public static final int MAXIMIZE = 1;

        @NoInline
        public static interface WindowMaxmizeCallback extends Callback {
            void callback(saucer_handle saucer, boolean b);
        }

        /* ------------------------ */
        /* ------------------------ */
        /* ------------------------ */

        /** Requires {@link WindowMinimizeCallback} */
        public static final int MINIMIZE = 2;

        @NoInline
        public static interface WindowMinimizeCallback extends Callback {
            void callback(saucer_handle saucer, boolean b);
        }

        /* ------------------------ */
        /* ------------------------ */
        /* ------------------------ */

        /** Requires {@link WindowClosedCallback} */
        public static final int CLOSED = 3;

        @NoInline
        public static interface WindowClosedCallback extends Callback {
            void callback(saucer_handle saucer);
        }

        /* ------------------------ */
        /* ------------------------ */
        /* ------------------------ */

        /** Requires {@link WindowResizeEventCallback} */
        public static final int RESIZE = 4;

        @NoInline
        public static interface WindowResizeEventCallback extends Callback {
            void callback(saucer_handle saucer, int width, int height);
        }

        /* ------------------------ */
        /* ------------------------ */
        /* ------------------------ */

        /** Requires {@link WindowFocusCallback} */
        public static final int FOCUS = 5;

        @NoInline
        public static interface WindowFocusCallback extends Callback {
            void callback(saucer_handle saucer, boolean b);
        }

        /* ------------------------ */
        /* ------------------------ */
        /* ------------------------ */

        /** Requires {@link WindowCloseRequestCallback} */
        public static final int CLOSE = 6;

        @NoInline
        public static interface WindowCloseRequestCallback extends Callback {
            /**
             * @return {@link SAUCER_POLICY}
             */
            int callback(saucer_handle saucer);
        }
    };

    /**
     * bitmask
     */
    public static class SAUCER_WINDOW_EDGE {
        public static final int TOP = 1 << 0;
        public static final int BOTTOM = 1 << 1;
        public static final int LEFT = 1 << 2;
        public static final int RIGHT = 1 << 3;
    }

    public static class SAUCER_POLICY {
        public static final int ALLOW = 0;
        public static final int BLOCK = 1;
    }

    @RequiresFree
    public static class saucer_handle extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_handle();
        }

        @Override
        protected void free() {
            _webview.N.saucer_free(this);
        }
    }

    public boolean saucer_window_visible(saucer_handle _instance);

    public boolean saucer_window_focused(saucer_handle _instance);

    public boolean saucer_window_minimized(saucer_handle _instance);

    public boolean saucer_window_maximized(saucer_handle _instance);

    public boolean saucer_window_resizable(saucer_handle _instance);

    public boolean saucer_window_decorations(saucer_handle _instance);

    public boolean saucer_window_always_on_top(saucer_handle _instance);

    public boolean saucer_window_click_through(saucer_handle _instance);

    public @RequiresFree SaucerPointerReference<String> saucer_window_title(saucer_handle _instance);

    public void saucer_window_size(saucer_handle _instance, @RequiresFree SaucerPointerReference<Integer> width, @RequiresFree SaucerPointerReference<Integer> height);

    public void saucer_window_max_size(saucer_handle _instance, @RequiresFree SaucerPointerReference<Integer> width, @RequiresFree SaucerPointerReference<Integer> height);

    public void saucer_window_min_size(saucer_handle _instance, @RequiresFree SaucerPointerReference<Integer> width, @RequiresFree SaucerPointerReference<Integer> height);

    public void saucer_window_hide(saucer_handle _instance);

    public void saucer_window_show(saucer_handle _instance);

    public void saucer_window_close(saucer_handle _instance);

    public void saucer_window_focus(saucer_handle _instance);

    public void saucer_window_start_drag(saucer_handle _instance);

    /**
     * @param edge {@link SAUCER_WINDOW_EDGE}
     */
    public void saucer_window_start_resize(saucer_handle _instance, int edge);

    public void saucer_window_set_minimized(saucer_handle _instance, boolean enabled);

    public void saucer_window_set_maximized(saucer_handle _instance, boolean enabled);

    public void saucer_window_set_resizable(saucer_handle _instance, boolean enabled);

    public void saucer_window_set_decorations(saucer_handle _instance, boolean enabled);

    public void saucer_window_set_always_on_top(saucer_handle _instance, boolean enabled);

    public void saucer_window_set_click_through(saucer_handle _instance, boolean enabled);

    public void saucer_window_set_icon(saucer_handle _instance, saucer_icon icon);

    public void saucer_window_set_title(saucer_handle _instance, String title);

    public void saucer_window_set_size(saucer_handle _instance, int width, int height);

    public void saucer_window_set_max_size(saucer_handle _instance, int width, int height);

    public void saucer_window_set_min_size(saucer_handle _instance, int width, int height);

    /**
     * @param event {@link SAUCER_WINDOW_EVENT}
     */
    public void saucer_window_clear(saucer_handle _instance, int event);

    /**
     * @param event {@link SAUCER_WINDOW_EVENT}
     */
    public void saucer_window_remove(saucer_handle _instance, int event, long id);

    /**
     * @param event    {@link SAUCER_WINDOW_EVENT}
     * @param callback One of the callbacks required by the provided event.
     */
    public void saucer_window_once(saucer_handle _instance, int event, Callback callback);

    /**
     * @param event    {@link SAUCER_WINDOW_EVENT}
     * @param callback One of the callbacks required by the provided event.
     */
    public long saucer_window_on(saucer_handle _instance, int event, Callback callback);

}
