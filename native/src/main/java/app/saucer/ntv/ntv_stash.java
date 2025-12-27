package app.saucer.ntv;

import com.sun.jna.Callback;
import com.sun.jna.Library;

import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerType;
import app.saucer.ntv.util.size_t;

public interface ntv_stash extends Library {
    public static final ntv_stash N = SaucerNativeLoader.load(ntv_stash.class);

    public static class saucer_stash extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_stash();
        }

        @Override
        protected void free() {
            N.saucer_stash_free(this);
        }
    }

    public static interface saucer_stash_lazy_callback extends Callback {
        void callback(Callback arg0);
    }

    public byte[] saucer_stash_data(saucer_stash arg0);

    public size_t saucer_stash_size(saucer_stash arg0);

    public void saucer_stash_free(saucer_stash arg0);

    public saucer_stash saucer_stash_copy(saucer_stash arg0);

    public saucer_stash saucer_stash_new_from(byte[] arg0, size_t arg1);

    public saucer_stash saucer_stash_new_view(byte[] arg0, size_t arg1);

    public saucer_stash saucer_stash_new_lazy(saucer_stash_lazy_callback arg0, Callback userdata);

    public saucer_stash saucer_stash_new_from_str(String arg0);

    public saucer_stash saucer_stash_new_view_str(String arg0);

    public saucer_stash saucer_stash_new_empty();

}
