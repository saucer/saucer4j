package app.saucer.ntv;

import com.sun.jna.Library;

import app.saucer.ntv.documentation.RequiresFree;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerType;
import app.saucer.ntv.util.size_t;

public interface _options extends Library {
    public static final _options N = SaucerNativeLoader.load(_options.class);

    @RequiresFree
    public static class saucer_options extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_options();
        }

        @Override
        protected void free() {
            N.saucer_options_free(this);
        }
    }

    public @RequiresFree saucer_options saucer_options_new(String id);

    public void saucer_options_free(saucer_options _instance);

    public void saucer_options_set_argc(saucer_options _instance, int argc);

    public void saucer_options_set_argv(saucer_options _instance, String[] argv);

    public void saucer_options_set_threads(saucer_options _instance, size_t threads);

}
