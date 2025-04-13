package app.saucer.ntv;

import com.sun.jna.Library;

import app.saucer.ntv.documentation.RequiresFree;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerReference;
import app.saucer.ntv.util.SaucerPointerType;

public interface _navigation extends Library {
    public static final _navigation N = SaucerNativeLoader.load(_navigation.class);

    @RequiresFree
    public static class saucer_navigation extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_navigation();
        }

        @Override
        protected void free() {
            N.saucer_navigation_free(this);
        }
    }

    public void saucer_navigation_free(saucer_navigation _instance);

    public @RequiresFree SaucerPointerReference<String> saucer_navigation_url(saucer_navigation _instance);

    public boolean saucer_navigation_new_window(saucer_navigation _instance);

    public boolean saucer_navigation_redirection(saucer_navigation _instance);

    public boolean saucer_navigation_user_initiated(saucer_navigation _instance);

}
