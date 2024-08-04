package co.casterlabs.saucer.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import co.casterlabs.saucer._impl._SafePointer;
import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer.documentation.InternalUseOnly;
import co.casterlabs.saucer.documentation.PointerType;
import lombok.NonNull;

@PointerType
@SuppressWarnings("deprecation")
public final class SaucerIcon extends _SafePointer {
    private static final _Native N = _SaucerNative.load(_Native.class);

    @Deprecated
    @InternalUseOnly
    public SaucerIcon(@NonNull Pointer pointer) {
        super.setupPointer(pointer, N::saucer_icon_free);
    }

    public SaucerIcon of(@NonNull byte[] data) {
        _SafePointer $$result = _SafePointer.allocate(Native.POINTER_SIZE);
        N.saucer_icon_from_data($$result.p(), new SaucerStash(data).p());

        Pointer $result = $$result.p().getPointer(0);
        return new SaucerIcon($result);
    }

    public boolean isEmpty() {
        return N.saucer_icon_empty(this.p());
    }

    // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/icon.h
    static interface _Native extends Library {

        void saucer_icon_from_data(Pointer $$result, Pointer $data);

        void saucer_icon_free(Pointer $instance);

        boolean saucer_icon_empty(Pointer $instance);

    }

}
