package co.casterlabs.saucer.scheme;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer._impl._SafePointer;
import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer.documentation.InternalUseOnly;
import co.casterlabs.saucer.documentation.NoFree;
import co.casterlabs.saucer.documentation.PointerType;
import lombok.Getter;
import lombok.NonNull;

@Getter
@PointerType
@SuppressWarnings("deprecation")
public final class SaucerSchemeResponse extends _SafePointer {
    private static final _Native N = _SaucerNative.load(_Native.class);

    private SaucerStash data; // Avoid it getting GC'd before we're ready

    @Deprecated
    @InternalUseOnly
    public SaucerSchemeResponse(@NonNull Pointer pointer) {
        super.setupPointer(pointer, N::saucer_response_free);
    }

    public SaucerSchemeResponse(@NonNull SaucerStash data, @NonNull String mimeType) {
        this(N.saucer_response_new(data.p(), _SafePointer.allocate(mimeType)));
        this.data = data;
        data.freeIsExternalNow();
    }

    public SaucerSchemeResponse(@NonNull SaucerRequestError error) {
        this(N.saucer_response_unexpected(error.ordinal()));
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerSchemeResponse header(@NonNull String key, @NonNull String value) {
        N.saucer_response_add_header(this, _SafePointer.allocate(key), _SafePointer.allocate(value));
        return this;
    }

    public static enum SaucerRequestError {
        SAUCER_REQUEST_ERROR_FAILED,
        SAUCER_REQUEST_ERROR_DENIED,
        SAUCER_REQUEST_ERROR_ABORTED,
        SAUCER_REQUEST_ERROR_BAD_URL,
        SAUCER_REQUEST_ERROR_NOT_FOUND,
    }

    // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/scheme.h
    static interface _Native extends Library {

        @NoFree
        Pointer saucer_response_new(Pointer $stash, _SafePointer $mime);

        @NoFree
        Pointer saucer_response_unexpected(int error);

        void saucer_response_free(Pointer $instance);

        void saucer_response_add_header(_SafePointer $instance, _SafePointer $key, _SafePointer $value);

    }

}
