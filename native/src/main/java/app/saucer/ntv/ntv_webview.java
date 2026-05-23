package app.saucer.ntv;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

import app.saucer.ntv.ntv_icon.saucer_icon;
import app.saucer.ntv.ntv_navigation.saucer_navigation;
import app.saucer.ntv.ntv_permission.saucer_permission_request;
import app.saucer.ntv.ntv_scheme.saucer_scheme_handler;
import app.saucer.ntv.ntv_stash.saucer_stash;
import app.saucer.ntv.ntv_url.saucer_url;
import app.saucer.ntv.ntv_window.saucer_window;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerType;
import app.saucer.ntv.util.size_t;

public interface ntv_webview extends Library {
    public static final ntv_webview N = SaucerNativeLoader.load(ntv_webview.class);

    public static class saucer_webview extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_webview();
        }

        @Override
        protected void free() {
            N.saucer_webview_free(this);
        }
    }

    public static class saucer_webview_options extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_webview_options();
        }

        @Override
        protected void free() {
            N.saucer_webview_options_free(this);
        }
    }

    /**
     * @remark The passed permission requests lifetime ends when the event-callback
     *         finishes. To keep it around, it has to be explictly copied!
     */
    public static interface saucer_webview_event_permission extends Callback {
        /*saucer_status*/int callback(saucer_webview arg0, saucer_permission_request arg1, Pointer arg2);
    }

    public static interface saucer_webview_event_fullscreen extends Callback {
        /*saucer_policy*/int callback(saucer_webview arg0, boolean arg1, Pointer arg2);
    }

    public static interface saucer_webview_event_dom_ready extends Callback {
        void callback(saucer_webview arg0, Pointer arg1);
    }

    /**
     * @remark The passed urls lifetime ends when the event-callback finishes. To
     *         keep it around, it has to be explictly copied!
     */
    public static interface saucer_webview_event_navigated extends Callback {
        void callback(saucer_webview arg0, saucer_url arg1, Pointer arg2);
    }

    /**
     * @remark The passed navigations lifetime ends when the event-callback
     *         finishes. It cannot be copied.
     */
    public static interface saucer_webview_event_navigate extends Callback {
        /*saucer_policy*/int callback(saucer_webview arg0, saucer_navigation arg1, Pointer arg2);
    }

    public static interface saucer_webview_event_message extends Callback {
        /*saucer_status*/int callback(saucer_webview arg0, String arg1, size_t arg2, Pointer arg3);
    }

    /**
     * @remark The passed urls lifetime ends when the event-callback finishes. To
     *         keep it around, it has to be explictly copied!
     */
    public static interface saucer_webview_event_request extends Callback {
        void callback(saucer_webview arg0, saucer_url arg1, Pointer arg2);
    }

    /**
     * @remark The passed icons lifetime ends when the event-callback finishes. To
     *         keep it around, it has to be explictly copied!
     */
    public static interface saucer_webview_event_favicon extends Callback {
        void callback(saucer_webview arg0, saucer_icon arg1, Pointer arg2);
    }

    public static interface saucer_webview_event_title extends Callback {
        void callback(saucer_webview arg0, String arg1, size_t arg2, Pointer arg3);
    }

    public static interface saucer_webview_event_load extends Callback {
        void callback(saucer_webview arg0, /*saucer_state*/int arg1, Pointer arg2);
    }

    public static class saucer_state {

        public static final int STARTED = 0;

        public static final int FINISHED = 1;
    };

    public static class saucer_status {

        public static final int HANDLED = 0;

        public static final int UNHANDLED = 1;
    };

    public static class saucer_script_time {

        public static final int CREATION = 0;

        public static final int READY = 1;
    };

    public static class saucer_webview_event {

        public static final int PERMISSION = 0;

        public static final int FULLSCREEN = 1;

        public static final int DOM_READY = 2;

        public static final int NAVIGATED = 3;

        public static final int NAVIGATE = 4;

        public static final int MESSAGE = 5;

        public static final int REQUEST = 6;

        public static final int FAVICON = 7;

        public static final int TITLE = 8;

        public static final int LOAD = 9;
    };

    public void saucer_webview_options_free(saucer_webview_options arg0);

    public saucer_webview_options saucer_webview_options_new(saucer_window arg0);

    public void saucer_webview_options_set_attributes(saucer_webview_options arg0, boolean arg1);

    public void saucer_webview_options_set_persistent_cookies(saucer_webview_options arg0, boolean arg1);

    public void saucer_webview_options_set_hardware_acceleration(saucer_webview_options arg0, boolean arg1);

    public void saucer_webview_options_set_storage_path(saucer_webview_options arg0, String arg1);

    public void saucer_webview_options_set_user_agent(saucer_webview_options arg0, String arg1);

    public void saucer_webview_options_append_browser_flag(saucer_webview_options arg0, String arg1);

    public void saucer_webview_free(saucer_webview arg0);

    /** @note The pointer passed to @param {error} can be null */
    public saucer_webview saucer_webview_new(saucer_webview_options arg0, IntByReference error);

    public saucer_url saucer_webview_url(saucer_webview arg0);

    public saucer_icon saucer_webview_favicon(saucer_webview arg0);

    public void saucer_webview_page_title(saucer_webview arg0, /*string*/byte[] arg1, size_t.ByReference arg2);

    public boolean saucer_webview_dev_tools(saucer_webview arg0);

    public boolean saucer_webview_context_menu(saucer_webview arg0);

    public boolean saucer_webview_force_dark(saucer_webview arg0);

    public void saucer_webview_background(saucer_webview arg0, ByteByReference r, ByteByReference g, ByteByReference b, ByteByReference a);

    public void saucer_webview_bounds(saucer_webview arg0, IntByReference x, IntByReference y, IntByReference w, IntByReference h);

    public void saucer_webview_set_url(saucer_webview arg0, saucer_url arg1);

    public void saucer_webview_set_url_str(saucer_webview arg0, String arg1);

    public void saucer_webview_set_html(saucer_webview arg0, String arg1);

    public void saucer_webview_set_dev_tools(saucer_webview arg0, boolean arg1);

    public void saucer_webview_set_context_menu(saucer_webview arg0, boolean arg1);

    public void saucer_webview_set_force_dark(saucer_webview arg0, boolean arg1);

    public void saucer_webview_set_background(saucer_webview arg0, /*unsigned*/byte r, /*unsigned*/byte g, /*unsigned*/byte b, /*unsigned*/byte a);

    public void saucer_webview_reset_bounds(saucer_webview arg0);

    public void saucer_webview_set_bounds(saucer_webview arg0, int x, int y, int w, int h);

    public void saucer_webview_back(saucer_webview arg0);

    public void saucer_webview_forward(saucer_webview arg0);

    public void saucer_webview_reload(saucer_webview arg0);

    public void saucer_webview_serve(saucer_webview arg0, String arg1);

    public void saucer_webview_embed(saucer_webview arg0, String path, saucer_stash content, String mime);

    public void saucer_webview_unembed_all(saucer_webview arg0);

    public void saucer_webview_unembed(saucer_webview arg0, String arg1);

    public void saucer_webview_execute(saucer_webview arg0, String arg1);

    public size_t saucer_webview_inject(saucer_webview arg0, String code, /*saucer_script_time*/int run_at, boolean no_frames, boolean clearable);

    public void saucer_webview_uninject_all(saucer_webview arg0);

    public void saucer_webview_uninject(saucer_webview arg0, size_t arg1);

    public void saucer_webview_handle_scheme(saucer_webview arg0, String arg1, saucer_scheme_handler arg2, Pointer userdata);

    public void saucer_webview_remove_scheme(saucer_webview arg0, String arg1);

    public size_t saucer_webview_on(saucer_webview arg0, /*saucer_webview_event*/int arg1, Callback callback, boolean clearable, Pointer userdata);

    public void saucer_webview_once(saucer_webview arg0, /*saucer_webview_event*/int arg1, Callback callback, Pointer userdata);

    public void saucer_webview_off(saucer_webview arg0, /*saucer_webview_event*/int arg1, size_t arg2);

    public void saucer_webview_off_all(saucer_webview arg0, /*saucer_webview_event*/int arg1);

    public void saucer_webview_register_scheme(String arg0);

    /**
     * @note Please refer to the documentation in `application.h` on how to use this
     *       function.
     */
    public void saucer_webview_native(saucer_webview arg0, size_t arg1, Pointer arg2, size_t.ByReference arg3);

}
