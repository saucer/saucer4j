package app.saucer.nativebindings.util;

import com.sun.jna.FromNativeContext;
import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;

public abstract class SaucerPointerType implements NativeMapped, AutoCloseable {
    protected Pointer $ref;

    protected abstract SaucerPointerType newInstance();

    public abstract void free();

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    @Override
    public final Object fromNative(Object nativeValue, FromNativeContext context) {
        if (nativeValue == null) {
            return null;
        } else {
            SaucerPointerType instance = this.newInstance();
            instance.$ref = (Pointer) nativeValue;
            return instance;
        }
    }

    @Override
    public final Object toNative() {
        return $ref;
    }

    @Override
    public final Class<?> nativeType() {
        return Pointer.class;
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * For compatibility with Java's try-with-resources.
     */
    @Override
    public final void close() {
        this.free();
    }

}
