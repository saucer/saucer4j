package app.saucer._impl.c;

import com.sun.jna.FromNativeContext;
import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;

import app.saucer.documentation.InternalUseOnly;

@SuppressWarnings("deprecation")
@Deprecated
@InternalUseOnly
public abstract class SaucerPointerType<T extends SaucerPointerType<?>> implements NativeMapped {
    protected Pointer $ref;
    private boolean autoFree = true;

    protected abstract T newInstanceForJNA();

    protected abstract void free();

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * @implNote Override as needed.
     */
    protected void disableAutoFree() {
        this.autoFree = false;
    }

    @Override
    protected void finalize() throws Throwable {
        if (this.autoFree) {
            this.free();
        }
    }

    @Override
    public final Object fromNative(Object nativeValue, FromNativeContext context) {
        if (nativeValue == null) {
            return null;
        } else {
            T instance = newInstanceForJNA();
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

}
