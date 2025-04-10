package app.saucer.nativebindings;

import com.sun.jna.Library;

import app.saucer.nativebindings.documentation.RequiresFree;
import app.saucer.nativebindings.util.SaucerNativeLoader;
import app.saucer.nativebindings.util.SaucerPointerType;
import app.saucer.nativebindings.util.size_t;

public interface _options extends Library {
    public static final _options N = SaucerNativeLoader.load(_options.class);

    @RequiresFree
    public static class saucer_options extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_options();
        }

        @Override
        public void free() {
            N.saucer_options_free(this);
        }
    }

    public @RequiresFree saucer_options saucer_options_new(String id);

    public void saucer_options_free(saucer_options _instance);

    public void saucer_options_set_argc(saucer_options _instance, int argc);

    public void saucer_options_set_argv(saucer_options _instance, String[] argv);

    public void saucer_options_set_threads(saucer_options _instance, size_t threads);

}
