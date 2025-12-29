package app.saucer.webview.scheme;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import app.saucer.ntv.ntv_scheme;
import app.saucer.ntv.ntv_scheme.saucer_scheme_request;
import app.saucer.ntv.ntv_stash;
import app.saucer.ntv.ntv_stash.saucer_stash;
import app.saucer.ntv.ntv_url.saucer_url;
import app.saucer.ntv.documentation.InternalUseOnly;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.size_t;
import app.saucer.util.SaucerUrl;

@SuppressWarnings("deprecation")
public final class SaucerSchemeRequest extends SaucerBoxedType<saucer_scheme_request> {

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerSchemeRequest(saucer_scheme_request $ref, boolean autoFree) {
        super($ref, autoFree);
    }

    @Override
    public SaucerSchemeRequest clone() {
        saucer_scheme_request cpy = ntv_scheme.N.saucer_scheme_request_copy($ref);
        return new SaucerSchemeRequest(cpy, true);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * @return The HTTP method of the request (e.g., "GET", "POST").
     */
    public String method() {
        size_t.ByReference sizeRef = new size_t.ByReference();

        // First call to get the size
        ntv_scheme.N.saucer_scheme_request_method($ref, null, sizeRef);

        // Second call to get the actual string
        byte[] buffer = new byte[sizeRef.getValue().intValue()];
        ntv_scheme.N.saucer_scheme_request_method($ref, buffer, sizeRef);

        return new String(buffer, StandardCharsets.UTF_8);
    }

    /**
     * @return The URL of the request.
     */
    public SaucerUrl url() {
        saucer_url $url = ntv_scheme.N.saucer_scheme_request_url($ref);
        return new SaucerUrl($url, false);
    }

    /**
     * @return The content/body of the request if any.
     */
    public byte[] content() {
        try (saucer_stash stash = ntv_scheme.N.saucer_scheme_request_content($ref)) {
            return ntv_stash.N.saucer_stash_data(stash);
        }
    }

    /**
     * @return A map of all headers in the request.
     */
    public Map<String, String> headers() {
        // Headers are returned null delimited,
        // e.g. as "Header: Value\0Another Header: Value"
        size_t.ByReference sizeRef = new size_t.ByReference();

        // First call to get the size
        ntv_scheme.N.saucer_scheme_request_headers($ref, null, sizeRef);

        // Second call to get the actual string
        byte[] buffer = new byte[sizeRef.getValue().intValue()];
        ntv_scheme.N.saucer_scheme_request_headers($ref, buffer, sizeRef);

        Map<String, String> headersMap = new HashMap<>();

        StringBuilder keyBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();
        boolean readingKey = true;
        for (int i = 0; i < buffer.length; i++) {
            byte b = buffer[i];
            if (b == 0) {
                // End of a header
                if (keyBuilder.length() > 0) {
                    headersMap.put(keyBuilder.toString().trim(), valueBuilder.toString().trim());
                    keyBuilder.setLength(0);
                    valueBuilder.setLength(0);
                }
                readingKey = true;
                continue;

            }

            if (b == ':' && readingKey) {
                // Switch to reading value
                readingKey = false;
                continue;
            }

            // Append character to key or value
            if (readingKey) {
                keyBuilder.append((char) b);
            } else {
                valueBuilder.append((char) b);
            }
        }

        return headersMap;
    }

}
