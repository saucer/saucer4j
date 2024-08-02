package co.casterlabs.saucer.scheme;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer._impl._SafePointer;
import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer._impl._SaucerNative.size_t;
import co.casterlabs.saucer.documentation.InternalUseOnly;
import co.casterlabs.saucer.documentation.PointerType;
import lombok.NonNull;

@PointerType
@SuppressWarnings("deprecation")
public final class SaucerStash extends _SafePointer {
    private static final _Native N = _SaucerNative.load(_Native.class);

    @Deprecated
    @InternalUseOnly
    public SaucerStash(@NonNull Pointer pointer) {
        super.setupPointer(pointer, N::saucer_stash_free);
    }

    public SaucerStash(@NonNull byte[] data) {
        this(N.saucer_stash_from(data, new size_t(data.length)));
    }

    public int size() {
        return N.saucer_stash_size(this.p()).intValue();
    }

    public byte[] data() {
        Pointer pointer = N.saucer_stash_data(this.p());
        int size = this.size();
        return pointer.getByteArray(0, size);
    }

    @InternalUseOnly
    @Deprecated
    @Override
    protected Pointer p() {
        return super.p();
    }

    @InternalUseOnly
    @Deprecated
    @Override
    protected void freeIsExternalNow() {
        super.freeIsExternalNow();
    }

    // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/stash.h
    static interface _Native extends Library {

        Pointer saucer_stash_from(byte[] data, size_t size);

        void saucer_stash_free(Pointer $instance);

        size_t saucer_stash_size(Pointer $instance);

        Pointer saucer_stash_data(Pointer $instance);

    }

}
