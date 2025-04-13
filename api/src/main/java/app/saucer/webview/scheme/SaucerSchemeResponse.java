package app.saucer.webview.scheme;

import app.saucer.documentation.InternalUseOnly;
import app.saucer.ntv._scheme;
import app.saucer.ntv._scheme.saucer_scheme_response;
import app.saucer.ntv._stash;
import app.saucer.ntv._stash.saucer_stash;
import app.saucer.ntv.documentation.RequiresFree;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.size_t;
import lombok.NonNull;

public final class SaucerSchemeResponse extends SaucerBoxedType<saucer_scheme_response> {

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerSchemeResponse(saucer_scheme_response $ref) {
        super($ref);
    }

    public static SaucerSchemeResponse create(@NonNull byte[] data, @NonNull String mimeType) {
        @RequiresFree
        saucer_stash stash = _stash.N.saucer_stash_from(data, new size_t(data.length));
        try {
            saucer_scheme_response response = _scheme.N.saucer_scheme_response_new(stash, mimeType);
            return new SaucerSchemeResponse(response);
        } finally {
            _stash.N.saucer_stash_free(stash);
        }
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * @return this instance, for chaining.
     */
    public SaucerSchemeResponse status(int statusCode) {
        _scheme.N.saucer_scheme_response_set_status($ref, statusCode);
        return this;
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerSchemeResponse header(@NonNull String key, @NonNull String value) {
        _scheme.N.saucer_scheme_response_add_header($ref, key, value);
        return this;
    }

}
