package app.saucer.scheme;

import com.sun.jna.Library;

import app.saucer._impl._SaucerNative;
import app.saucer._impl.c.NoFree;
import app.saucer._impl.c.SaucerPointerType;
import app.saucer.documentation.InternalUseOnly;
import app.saucer.utils.SaucerStash;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("deprecation")
public final class SaucerSchemeResponse extends SaucerPointerType<SaucerSchemeResponse> {
    private static final _Native N = _SaucerNative.load(_Native.class);

    @Deprecated
    @InternalUseOnly
    public SaucerSchemeResponse() {
        // NOOP constructor for JNA.
    }

    @Override
    protected SaucerSchemeResponse newInstanceForJNA() {
        return new SaucerSchemeResponse();
    }

    @Override
    protected void free() {
        N.saucer_scheme_response_free(this);
    }

    @Override
    public void disableAutoFree() {
        super.disableAutoFree();
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public static SaucerSchemeResponse success(@NonNull SaucerStash data, @NonNull String mimeType) {
        return N.saucer_scheme_response_new(data, mimeType);
    }

    public static SaucerSchemeResponse error(@NonNull SaucerRequestError error) {
        return N.saucer_scheme_response_unexpected(error.ordinal());
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * @return this instance, for chaining.
     */
    public SaucerSchemeResponse status(int statusCode) {
        N.saucer_scheme_response_set_status(this, statusCode);
        return this;
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerSchemeResponse header(@NonNull String key, @NonNull String value) {
        N.saucer_scheme_response_add_header(this, key, value);
        return this;
    }

    /* -------------------------1----------- */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public static enum SaucerRequestError {
        NOT_FOUND,
        INVALID,
        ABORTED,
        DENIED,
        FAILED,
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    // https://github.com/saucer/bindings/blob/main/include/saucer/scheme.h
    static interface _Native extends Library {

        @NoFree
        SaucerSchemeResponse saucer_scheme_response_new(SaucerStash stash, String mime);

        @NoFree
        SaucerSchemeResponse saucer_scheme_response_unexpected(int error);

        /**
         * @apiNote Under normal circumstances this function should not be used. Once a
         *          saucer_response is returned from within a saucer_scheme_handler it
         *          is automatically deleted. You may use this function to free the
         *          response in case of an exception.
         */
        void saucer_scheme_response_free(SaucerSchemeResponse instance);

        void saucer_scheme_response_add_header(SaucerSchemeResponse instance, String key, String value);

        void saucer_scheme_response_set_status(SaucerSchemeResponse instance, int statusCode);

    }

}
