package app.saucer.ntv;

import com.sun.jna.Library;

import app.saucer.ntv.ntv_app.saucer_application;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerType;

public interface ntv_loop extends Library {
    public static final ntv_loop N = SaucerNativeLoader.load(ntv_loop.class, "loop");

    public static class saucer_loop extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_loop();
        }

        @Override
        protected void free() {
            N.saucer_loop_free(this);
        }
    }

    public int saucer_loop_free(saucer_loop arg0);

    public saucer_loop saucer_loop_new(saucer_application arg0);

    public int saucer_loop_run(saucer_loop arg0);

    public int saucer_loop_iteration(saucer_loop arg0);

    public int saucer_loop_quit(saucer_loop arg0);

}
