package co.casterlabs.saucer.impl;

import java.util.function.Consumer;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.documentation.InternalUseOnly;
import lombok.NonNull;

/**
 * A slightly-safer pointer reference that can call a custom free().
 */
@Deprecated
@InternalUseOnly
public class _SaucerPointer {
    private Pointer $ptr;
    private Consumer<Pointer> free;
    private boolean hasBeenFreed = true; // Initial state.

    /**
     * @implSpec Sub-classes MUST call {@link #setup(Pointer, Consumer)}!
     */
    protected _SaucerPointer() {}

    protected void setup(@NonNull Pointer $ptr, @NonNull Consumer<Pointer> free) {
        this.$ptr = $ptr;
        this.free = free;
        this.hasBeenFreed = false;
    }

    /**
     * @implNote free() is just a standard c-free().
     */
    public _SaucerPointer(@NonNull Pointer $ptr) {
        this.setup($ptr, (p) -> Native.free(Pointer.nativeValue(p)));
    }

    /**
     * @implNote Your free consumer must handle everything itself.
     */
    public _SaucerPointer(@NonNull Pointer $ptr, @NonNull Consumer<Pointer> free) {
        this.setup($ptr, free);
    }

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

}
