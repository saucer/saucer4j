package app.saucer.ntv;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

import app.saucer.ntv.ntv_app.saucer_application;
import app.saucer.ntv.ntv_app.saucer_screen;
import app.saucer.ntv.ntv_icon.saucer_icon;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerType;
import app.saucer.ntv.util.size_t;

public interface ntv_window extends Library {
    public static final ntv_window N = SaucerNativeLoader.load(ntv_window.class);

    public static class saucer_window extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_window();
        }

        @Override
        protected void free() {
            N.saucer_window_free(this);
        }
    }

    public static interface saucer_window_event_decorated extends Callback {
        void callback(saucer_window arg0, /*saucer_window_decoration*/int arg1, Pointer arg2);
    }

    public static interface saucer_window_event_maximize extends Callback {
        void callback(saucer_window arg0, boolean arg1, Pointer arg2);
    }

    public static interface saucer_window_event_minimize extends Callback {
        void callback(saucer_window arg0, boolean arg1, Pointer arg2);
    }

    public static interface saucer_window_event_closed extends Callback {
        void callback(saucer_window arg0, Pointer arg1);
    }

    public static interface saucer_window_event_resize extends Callback {
        void callback(saucer_window arg0, int arg1, int arg2, Pointer arg3);
    }

    public static interface saucer_window_event_focus extends Callback {
        void callback(saucer_window arg0, boolean arg1, Pointer arg2);
    }

    public static interface saucer_window_event_close extends Callback {
        /*saucer_policy*/int callback(saucer_window arg0, Pointer arg1);
    }

    public static class saucer_window_edge {

        public static final int TOP = 1;

        public static final int BOTTOM = 2;

        public static final int LEFT = 4;

        public static final int RIGHT = 8;

        public static final int BOTTOM_LEFT = 6;

        public static final int BOTTOM_RIGHT = 10;

        public static final int TOP_LEFT = 5;

        public static final int TOP_RIGHT = 9;
    };

    public static class saucer_window_decoration {

        public static final int NONE = 0;

        public static final int PARTIAL = 1;

        public static final int FULL = 2;
    };

    public static class saucer_window_event {

        public static final int DECORATED = 0;

        public static final int MAXIMIZE = 1;

        public static final int MINIMIZE = 2;

        public static final int CLOSED = 3;

        public static final int RESIZE = 4;

        public static final int FOCUS = 5;

        public static final int CLOSE = 6;
    };

    public void saucer_window_free(saucer_window arg0);

    /** @note The pointer passed to @param {error} can be null */
    public saucer_window saucer_window_new(saucer_application arg0, IntByReference error);

    public boolean saucer_window_visible(saucer_window arg0);

    public boolean saucer_window_focused(saucer_window arg0);

    public boolean saucer_window_minimized(saucer_window arg0);

    public boolean saucer_window_maximized(saucer_window arg0);

    public boolean saucer_window_resizable(saucer_window arg0);

    public boolean saucer_window_fullscreen(saucer_window arg0);

    public boolean saucer_window_always_on_top(saucer_window arg0);

    public boolean saucer_window_click_through(saucer_window arg0);

    public void saucer_window_title(saucer_window arg0, /*string*/byte[] arg1, size_t.ByReference arg2);

    public void saucer_window_background(saucer_window arg0, ByteByReference r, ByteByReference g, ByteByReference b, ByteByReference a);

    public int saucer_window_decorations(saucer_window arg0);

    public void saucer_window_size(saucer_window arg0, IntByReference w, IntByReference h);

    public void saucer_window_max_size(saucer_window arg0, IntByReference w, IntByReference h);

    public void saucer_window_min_size(saucer_window arg0, IntByReference w, IntByReference h);

    public void saucer_window_position(saucer_window arg0, IntByReference x, IntByReference y);

    public saucer_screen saucer_window_screen(saucer_window arg0);

    public void saucer_window_hide(saucer_window arg0);

    public void saucer_window_show(saucer_window arg0);

    public void saucer_window_close(saucer_window arg0);

    public void saucer_window_focus(saucer_window arg0);

    public void saucer_window_start_drag(saucer_window arg0);

    public void saucer_window_start_resize(saucer_window arg0, /*saucer_window_edge*/int arg1);

    public void saucer_window_set_minimized(saucer_window arg0, boolean arg1);

    public void saucer_window_set_maximized(saucer_window arg0, boolean arg1);

    public void saucer_window_set_resizable(saucer_window arg0, boolean arg1);

    public void saucer_window_set_fullscreen(saucer_window arg0, boolean arg1);

    public void saucer_window_set_always_on_top(saucer_window arg0, boolean arg1);

    public void saucer_window_set_click_through(saucer_window arg0, boolean arg1);

    public void saucer_window_set_icon(saucer_window arg0, saucer_icon arg1);

    public void saucer_window_set_title(saucer_window arg0, String arg1);

    public void saucer_window_set_background(saucer_window arg0, /*unsigned*/byte r, /*unsigned*/byte g, /*unsigned*/byte b, /*unsigned*/byte a);

    public void saucer_window_set_decorations(saucer_window arg0, /*saucer_window_decoration*/int arg1);

    public void saucer_window_set_size(saucer_window arg0, int w, int h);

    public void saucer_window_set_max_size(saucer_window arg0, int w, int h);

    public void saucer_window_set_min_size(saucer_window arg0, int w, int h);

    public void saucer_window_set_position(saucer_window arg0, int x, int y);

    public size_t saucer_window_on(saucer_window arg0, /*saucer_window_event*/int arg1, Callback callback, boolean clearable, Pointer userdata);

    public void saucer_window_once(saucer_window arg0, /*saucer_window_event*/int arg1, Callback callback, Pointer userdata);

    public void saucer_window_off(saucer_window arg0, /*saucer_window_event*/int arg1, size_t arg2);

    public void saucer_window_off_all(saucer_window arg0, /*saucer_window_event*/int arg1);

    /**
     * @note Please refer to the documentation in `application.h` on how to use this
     *       function.
     */
    public void saucer_window_native(saucer_window arg0, size_t arg1, Pointer arg2, size_t.ByReference arg3);

}
