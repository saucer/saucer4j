package co.casterlabs.saucer._impl;

import java.util.function.Consumer;

import com.sun.jna.FromNativeContext;
import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.documentation.InternalUseOnly;
import lombok.NonNull;

/**
 * A pointer that automatically checks for use-after-frees and automatically
 * frees itself when no longer in use.
 * 
 * Note that this isn't always acceptable for long-lived references in native
 * code. In that cause, use {@link _SaucerNative#allocateUnsafe(int)} or
 * {@link _SaucerNative#allocateUnsafe(String)}.
 * 
 * @implSpec Sub-classes MUST call {@link #setupPointer(Pointer, Consumer)} or
 *           {@link #setupPointer(Pointer)}!
 */
@Deprecated
@InternalUseOnly
public class _SafePointer implements NativeMapped {
    private Pointer $ptr;
    private Consumer<Pointer> free;
    private boolean hasBeenFreed = true; // Initial state.

    protected void setupPointer(@NonNull Pointer $ptr, @NonNull Consumer<Pointer> free) {
        this.$ptr = $ptr;
        this.free = free == null ? _SaucerNative::free : free;
        this.hasBeenFreed = false;
    }

    /**
     * @implNote the default free() is Saucer's memory_free.
     */
    protected void setupPointer(@NonNull Pointer $ptr) {
        this.setupPointer($ptr, _SaucerNative::free);
    }

    /**
     * @implSpec Sub-classes MUST call {@link #setupPointer(Pointer, Consumer)} or
     *           {@link #setupPointer(Pointer)}!
     */
    public _SafePointer() {}

    /**
     * @implNote the default free() is Saucer's memory_free.
     */
    public static _SafePointer allocate(int size) {
        return of(_SaucerNative.allocateUnsafe(size), _SaucerNative::free);
    }

    /**
     * @implNote the default free() is Saucer's memory_free.
     */
    public static _SafePointer allocate(@NonNull String str) {
        return of(_SaucerNative.allocateUnsafe(str), _SaucerNative::free);
    }

    /**
     * @implNote the default free() is Saucer's memory_free.
     */
    public static _SafePointer of(@NonNull Pointer $ptr) {
        return of($ptr, _SaucerNative::free);
    }

    public static _SafePointer of(@NonNull Pointer $ptr, @NonNull Consumer<Pointer> free) {
        _SafePointer $sptr = new _SafePointer();
        $sptr.setupPointer($ptr, free);
        return $sptr;
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    protected void freeIsExternalNow() {
        this.free = (p) -> {
        };
    }

    protected final void free() {
        if (this.hasBeenFreed) return;
        this.free.accept($ptr);
        this.hasBeenFreed = true; // Import!
    }

    public Pointer p() {
        if (this.hasBeenFreed) {
            throw new IllegalStateException("Use after free!");
        }
        return $ptr;
    }

    @Override
    protected final void finalize() throws Throwable {
        this.free();
    }

    @Override
    public String toString() {
        if (this.hasBeenFreed) {
            return "<freed pointer>";
        } else {
            return String.format("<ptr @ %d>", Pointer.nativeValue($ptr));
        }
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    @Override
    public Object fromNative(Object nativeValue, FromNativeContext context) {
        if (nativeValue == null) {
            return null;
        } else {
            return of((Pointer) nativeValue);
        }
    }

    @Override
    public Object toNative() {
        return this.p();
    }

    @Override
    public Class<?> nativeType() {
        return Pointer.class;
    }

}
