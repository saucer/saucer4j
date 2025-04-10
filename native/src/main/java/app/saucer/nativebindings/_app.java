package app.saucer.nativebindings;

import com.sun.jna.Callback;
import com.sun.jna.Library;

import app.saucer.nativebindings._options.saucer_options;
import app.saucer.nativebindings.documentation.NoInline;
import app.saucer.nativebindings.documentation.RequiresFree;
import app.saucer.nativebindings.util.SaucerNativeLoader;
import app.saucer.nativebindings.util.SaucerPointerType;

public interface _app extends Library {
    public static final _app N = SaucerNativeLoader.load(_app.class);

    /**
     * @brief A handle to a saucer::application
     * 
     * @note  The application will live as long as there are handles to it. So make
     *        sure to properly free all handles you obtain to a saucer::application
     *        like through e.g. `saucer_application_active`!
     */
    @RequiresFree
    public static class saucer_application extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_application();
        }

        @Override
        public void free() {
            N.saucer_application_free(this);
        }
    }

    public @RequiresFree saucer_application saucer_application_init(saucer_options options);

    public void saucer_application_free(saucer_application _instance);

    public @RequiresFree saucer_application saucer_application_active();

    public boolean saucer_application_thread_safe(saucer_application _instance);

    @NoInline
    public static interface saucer_pool_callback extends Callback {
        void callback();
    }

    /**
     * @brief Submits (blocking) the given @param callback to the thread-pool
     */
    public void saucer_application_pool_submit(saucer_application _instance, saucer_pool_callback callback);

    /**
     * @brief Emplaces (non blocking) the given @param callback to the thread-pool
     */
    public void saucer_application_pool_emplace(saucer_application _instance, saucer_pool_callback callback);

    @NoInline
    public static interface saucer_post_callback extends Callback {
        void callback();
    }

    public void saucer_application_post(saucer_application _instance, saucer_post_callback callback);

    public void saucer_application_quit(saucer_application _instance);

    public void saucer_application_run(saucer_application _instance);

    public void saucer_application_run_once(saucer_application _instance);

}
