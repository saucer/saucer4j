package app.saucer.webview.scheme;

import java.util.Map;

import app.saucer.documentation.InternalUseOnly;
import app.saucer.ntv._scheme;
import app.saucer.ntv._scheme.saucer_scheme_request;
import app.saucer.ntv._stash;
import app.saucer.ntv._stash.saucer_stash;
import app.saucer.ntv.documentation.RequiresFree;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.SaucerPointerReference;

public final class SaucerSchemeRequest extends SaucerBoxedType<saucer_scheme_request> {

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerSchemeRequest(saucer_scheme_request $ref) {
        super($ref);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public String method() {
        try (SaucerPointerReference<String> str = _scheme.N.saucer_scheme_request_method($ref)) {
            return str.asString();
        }
    }

    public String url() {
        try (SaucerPointerReference<String> str = _scheme.N.saucer_scheme_request_url($ref)) {
            return str.asString();
        }
    }

    public byte[] content() {
        @RequiresFree
        saucer_stash stash = _scheme.N.saucer_scheme_request_content($ref);
        try {
            return _stash.N.saucer_stash_data(stash);
        } finally {
            _stash.N.saucer_stash_free(stash);
        }
    }

    public Map<String, String> headers() {
        return _scheme.saucer_scheme_request_headers($ref);
    }

}
