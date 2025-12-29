package app.saucer.ntv;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;

import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerType;
import app.saucer.ntv.util.size_t;

public interface ntv_app extends Library {
    public static final ntv_app N = SaucerNativeLoader.load(ntv_app.class);

    public static class saucer_screen extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_screen();
        }

        @Override
        protected void free() {
            N.saucer_screen_free(this);
        }
    }

    public static class saucer_application extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_application();
        }

        @Override
        protected void free() {
            N.saucer_application_free(this);
        }
    }

    public static class saucer_application_options extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_application_options();
        }

        @Override
        protected void free() {
            N.saucer_application_options_free(this);
        }
    }

    public static interface saucer_application_event_quit extends Callback {
        /*saucer_policy*/ int callback(saucer_application arg0, Callback arg1);
    }

    public static interface saucer_post_callback extends Callback {
        void callback(Callback arg0);
    }

    public static interface saucer_run_callback extends Callback {
        void callback(saucer_application arg0, Callback arg1);
    }

    public static interface saucer_finish_callback extends Callback {
        void callback(saucer_application arg0, Callback arg1);
    }

    public static class saucer_policy {

        public static final int ALLOW = 0;

        public static final int BLOCK = 1;
    };

    public static class saucer_application_event {

        public static final int QUIT = 0;
    };

    public void saucer_screen_free(saucer_screen arg0);

    public String saucer_screen_name(saucer_screen arg0);

    public void saucer_screen_size(saucer_screen arg0, IntByReference w, IntByReference h);

    public void saucer_screen_position(saucer_screen arg0, IntByReference x, IntByReference y);

    /**
     * @note The application options can be safely free'd after creating an
     *       application instance.
     */
    public void saucer_application_options_free(saucer_application_options arg0);

    public saucer_application_options saucer_application_options_new(String id);

    public void saucer_application_options_set_argc(saucer_application_options arg0, int arg1);

    public void saucer_application_options_set_argv(saucer_application_options arg0, /*string*/byte[][] arg1);

    public void saucer_application_options_set_quit_on_last_window_closed(saucer_application_options arg0, boolean arg1);

    /**
     * @attention Please call this after @see{saucer_application_run} returned and
     *            not in the finish callback or similar.
     */
    public void saucer_application_free(saucer_application arg0);

    public saucer_application saucer_application_new(saucer_application_options arg0, IntByReference error);

    public boolean saucer_application_thread_safe(saucer_application arg0);

    public void saucer_application_screens(saucer_application arg0, saucer_screen[] arg1, size_t.ByReference size);

    public void saucer_application_post(saucer_application arg0, saucer_post_callback arg1, Callback userdata);

    public void saucer_application_quit(saucer_application arg0);

    /**
     * @note This approximates the run function that uses coroutines. The run
     *       callback is called once the application is ready, then `co_await
     *       app->finish()` is called internally, afterwards, the finish callback is
     *       invoked. @attention You might want to use the loop module instead.
     */
    public int saucer_application_run(saucer_application arg0, saucer_run_callback arg1, saucer_finish_callback arg2, Callback userdata);

    public size_t saucer_application_on(saucer_application arg0, /*saucer_application_event*/int arg1, Callback callback, boolean clearable, Callback userdata);

    public void saucer_application_once(saucer_application arg0, /*saucer_application_event*/int arg1, Callback callback, Callback userdata);

    public void saucer_application_off(saucer_application arg0, /*saucer_application_event*/int arg1, size_t arg2);

    public void saucer_application_off_all(saucer_application arg0, /*saucer_application_event*/int arg1);

    /**
     * @brief Allows to access the stable natives of saucer::application. @param idx
     *        The index of the member to return, e.g. `0` to access the
     *        `AdwApplication *` of the webkitgtk natives. @param result A pointer
     *        to a buffer into which the member will be extracted. @param size The
     *        size of the buffer. @note To use this function, call it first
     *        with @param {result} being `nullptr`, and @param {size} pointing to a
     *        variable that will receive the required buffer size. Then call it
     *        again with @param {result} pointing to a buffer with sufficient size.
     *        Leave @param {size} unchanged in the second invocation.
     */
    public void saucer_application_native(saucer_application arg0, size_t idx, Callback result, size_t.ByReference size);

    /** @note The returned string does not need to be free'd. */
    public String saucer_version();

}
