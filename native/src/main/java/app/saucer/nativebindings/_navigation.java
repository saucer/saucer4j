package app.saucer.nativebindings;

import com.sun.jna.Library;

import app.saucer.nativebindings.documentation.RequiresFree;
import app.saucer.nativebindings.util.SaucerNativeLoader;
import app.saucer.nativebindings.util.SaucerPointerReference;
import app.saucer.nativebindings.util.SaucerPointerType;

public interface _navigation extends Library {
    public static final _navigation N = SaucerNativeLoader.load(_navigation.class);

    @RequiresFree
    public static class saucer_navigation extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_navigation();
        }

        @Override
        public void free() {
            N.saucer_navigation_free(this);
        }
    }

    public void saucer_navigation_free(saucer_navigation _instance);

    public @RequiresFree SaucerPointerReference<String> saucer_navigation_url(saucer_navigation _instance);

    public boolean saucer_navigation_new_window(saucer_navigation _instance);

    public boolean saucer_navigation_redirection(saucer_navigation _instance);

    public boolean saucer_navigation_user_initiated(saucer_navigation _instance);

}
