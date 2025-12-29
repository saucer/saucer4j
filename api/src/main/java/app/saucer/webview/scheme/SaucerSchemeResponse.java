package app.saucer.webview.scheme;

import app.saucer.ntv.ntv_scheme;
import app.saucer.ntv.ntv_scheme.saucer_scheme_response;
import app.saucer.ntv.ntv_stash;
import app.saucer.ntv.ntv_stash.saucer_stash;
import app.saucer.ntv.documentation.InternalUseOnly;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.size_t;
import lombok.NonNull;

public final class SaucerSchemeResponse extends SaucerBoxedType<saucer_scheme_response> {

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerSchemeResponse(saucer_scheme_response $ref, boolean autoFree) {
        super($ref, autoFree);
    }

    /**
     * Creates a new scheme response from raw data and a MIME type.
     * 
     * @see {@link MimeTypes} for looking up MIME types.
     */
    public static SaucerSchemeResponse create(@NonNull byte[] data, @NonNull String mimeType) {
        saucer_stash stash = ntv_stash.N.saucer_stash_new_from(data, new size_t(data.length));
//        try {
        saucer_scheme_response response = ntv_scheme.N.saucer_scheme_response_new(stash, mimeType);
        return new SaucerSchemeResponse(response, true);
        // do not free the stash, it's used by the response
//        } finally {
//            ntv_stash.N.saucer_stash_free(stash);
//        }
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * Sets the HTTP status code for this response.
     * 
     * @return this instance, for chaining.
     */
    public SaucerSchemeResponse status(int statusCode) {
        ntv_scheme.N.saucer_scheme_response_set_status($ref, statusCode);
        return this;
    }

    /**
     * Appends a header to this response.
     * 
     * @return this instance, for chaining.
     */
    public SaucerSchemeResponse appendHeader(@NonNull String key, @NonNull String value) {
        ntv_scheme.N.saucer_scheme_response_append_header($ref, key, value);
        return this;
    }

}
