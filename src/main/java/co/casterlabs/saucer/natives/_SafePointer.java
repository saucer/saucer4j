package co.casterlabs.saucer.natives;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.documentation.InternalUseOnly;
import lombok.NonNull;

/**
 * A pointer that automatically checks for use-after-frees and automatically
 * frees itself when no longer in use.
 */
@Deprecated
@InternalUseOnly
public class _SafePointer {
    private static final Consumer<Pointer> DEFAULT_FREE = (p) -> Native.free(Pointer.nativeValue(p));

    private Pointer $ptr;
    private Consumer<Pointer> free;
    private boolean hasBeenFreed = true; // Initial state.

    /**
     * @implNote the default free() (when you pass null) is a standard C free().
     */
    protected void setup(@NonNull Pointer $ptr, @Nullable Consumer<Pointer> free) {
        this.$ptr = $ptr;
        this.free = free == null ? DEFAULT_FREE : free;
        this.hasBeenFreed = false;
    }

    /**
     * @implSpec Sub-classes MUST call {@link #setup(Pointer, Consumer)}!
     */
    protected _SafePointer() {}

    public static _SafePointer allocate(int size) {
        Pointer $ptr = new Pointer(Native.malloc(size)); // NON GC'ABLE!
        $ptr.clear(size); // Zero the memory out. Safety first!
        return of($ptr);
    }

    public static _SafePointer of(@NonNull Pointer $ptr) {
        return of($ptr, null);
    }

    /**
     * @implNote the default free() (when you pass null) is a standard C free().
     */
    public static _SafePointer of(@NonNull Pointer $ptr, @Nullable Consumer<Pointer> free) {
        _SafePointer $sptr = new _SafePointer();
        $sptr.setup($ptr, free);
        return $sptr;
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    protected final void free() {
        if (this.hasBeenFreed) return;
        this.free.accept($ptr);
        this.hasBeenFreed = true; // Import!
    }

    protected final Pointer p() {
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

}
