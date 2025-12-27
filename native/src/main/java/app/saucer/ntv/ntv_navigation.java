package app.saucer.ntv;

import com.sun.jna.Library;

import app.saucer.ntv.ntv_url.saucer_url;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerType;

public interface ntv_navigation extends Library {
    public static final ntv_navigation N = SaucerNativeLoader.load(ntv_navigation.class);

    /** @remark A navigation cannot be copied. */
    public static class saucer_navigation extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_navigation();
        }

        @Override
        protected void free() {
//            N.saucer_navigation_free(this);
        }
    }

    public saucer_url saucer_navigation_url(saucer_navigation arg0);

    public boolean saucer_navigation_new_window(saucer_navigation arg0);

    public boolean saucer_navigation_redirection(saucer_navigation arg0);

    public boolean saucer_navigation_user_initiated(saucer_navigation arg0);

}
