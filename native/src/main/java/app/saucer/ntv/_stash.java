package app.saucer.ntv;

import com.sun.jna.Library;

import app.saucer.ntv.documentation.RequiresFree;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerType;
import app.saucer.ntv.util.size_t;

public interface _stash extends Library {
    public static final _stash N = SaucerNativeLoader.load(_stash.class);

    @RequiresFree
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

    public void saucer_stash_free(saucer_stash _instance);

    public size_t saucer_stash_size(saucer_stash _instance);

    public byte[] saucer_stash_data(saucer_stash _instance);

    public @RequiresFree saucer_stash saucer_stash_from(byte[] data, size_t size);

    public @RequiresFree saucer_stash saucer_stash_view(byte[] data, size_t size);

//    typedef saucer_stash _instance(*saucer_stash_lazy_callback)();
//
//    /**
//     * @note The stash returned from within the @param callback is automatically
//     *       deleted. However, the stash returned from this function must still be
//     *       free'd accordingly.
//     */
//    saucer_stash saucer_stash_lazy(saucer_stash_lazy_callback callback);

}
