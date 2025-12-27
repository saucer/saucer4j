package app.saucer.ntv.util;

import lombok.NonNull;

public abstract class SaucerBoxedType<T extends SaucerPointerType> {
    protected final T $ref;
    private boolean freeable = true;

    public SaucerBoxedType(@NonNull T $ref) {
        this.$ref = $ref;
    }

    /**
     * @deprecated Prevents this type from being free()'d. Very dangerous.
     */
    @Deprecated
    protected void noFree() {
        this.freeable = false;
    }

    @Override
    protected void finalize() {
        // TODO look at the Cleaner API. It might be a good replacement for this.
        if (this.freeable) {
            $ref.free();
        }
    }

    public static <R extends SaucerPointerType> R ntv(SaucerBoxedType<R> boxedType) {
        return boxedType.$ref;
    }

    /**
     * @deprecated Prevents this type from being free()'d. Very dangerous.
     */
    @Deprecated
    public static void noFree(SaucerBoxedType<?> boxedType) {
        boxedType.noFree();
    }

}
