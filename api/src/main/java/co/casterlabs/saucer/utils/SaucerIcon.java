package co.casterlabs.saucer.utils;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer._impl.c.SaucerPointerType;
import co.casterlabs.saucer._impl.c._SaucerMemory;
import co.casterlabs.saucer.documentation.InternalUseOnly;
import lombok.NonNull;

@SuppressWarnings("deprecation")
public final class SaucerIcon extends SaucerPointerType<SaucerIcon> {
    private static final _Native N = _SaucerNative.load(_Native.class);

    @Deprecated
    @InternalUseOnly
    public SaucerIcon() {
        // NOOP constructor for JNA.
    }

    @Override
    protected SaucerIcon newInstanceForJNA() {
        return new SaucerIcon();
    }

    @Override
    protected void free() {
        N.saucer_icon_free(this);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public @Nullable SaucerIcon of(@NonNull SaucerStash data) {
        Pointer $$result = _SaucerMemory.alloc(Native.POINTER_SIZE);
        N.saucer_icon_from_data($$result, data);

        Pointer $result = $$result.getPointer(0);
        _SaucerMemory.free($$result);

        if ($result == Pointer.NULL) {
            return null;
        }

        SaucerIcon icon = new SaucerIcon();
        icon.$ref = $result;
        return icon;
    }

    public boolean isEmpty() {
        return N.saucer_icon_empty(this);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    // https://github.com/saucer/bindings/blob/main/include/saucer/icon.h
    static interface _Native extends Library {

        void saucer_icon_from_data(Pointer $$result, SaucerStash data);

        void saucer_icon_free(SaucerIcon instance);

        boolean saucer_icon_empty(SaucerIcon instance);

    }

}
