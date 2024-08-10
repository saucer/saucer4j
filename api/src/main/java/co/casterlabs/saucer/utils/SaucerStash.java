package co.casterlabs.saucer.utils;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer._impl.c.SaucerPointerType;
import co.casterlabs.saucer._impl.c.size_t;
import co.casterlabs.saucer.documentation.InternalUseOnly;
import lombok.NonNull;

@SuppressWarnings("deprecation")
public final class SaucerStash extends SaucerPointerType<SaucerStash> {
    private static final _Native N = _SaucerNative.load(_Native.class);

    @Deprecated
    @InternalUseOnly
    public SaucerStash() {
        // NOOP constructor for JNA.
    }

    @Override
    protected SaucerStash newInstanceForJNA() {
        return new SaucerStash();
    }

    @Override
    protected void free() {
        N.saucer_stash_free(this);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public static SaucerStash of(@NonNull byte[] data) {
        return N.saucer_stash_from(data, new size_t(data.length));
    }

    public int size() {
        return N.saucer_stash_size(this).intValue();
    }

    public byte[] data() {
        // Do not free!
        Pointer pointer = N.saucer_stash_data(this);
        int size = this.size();
        return pointer.getByteArray(0, size);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    // https://github.com/saucer/bindings/blob/main/include/saucer/stash.h
    static interface _Native extends Library {

        SaucerStash saucer_stash_from(byte[] data, size_t size);

        void saucer_stash_free(SaucerStash instance);

        size_t saucer_stash_size(SaucerStash instance);

        Pointer saucer_stash_data(SaucerStash instance);

    }

}
