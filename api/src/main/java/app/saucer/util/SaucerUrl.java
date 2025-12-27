package app.saucer.util;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.ptr.IntByReference;

import app.saucer.ntv.ntv_url;
import app.saucer.ntv.ntv_url.saucer_url;
import app.saucer.ntv.documentation.InternalUseOnly;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.size_t;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonSerializer;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonString;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.NonNull;

@JsonClass(serializer = SaucerUrlSerializer.class)
public final class SaucerUrl extends SaucerBoxedType<saucer_url> {

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerUrl(@NonNull saucer_url $ref) {
        super($ref);
    }

    @Override
    public SaucerUrl clone() {
        saucer_url cpy = ntv_url.N.saucer_url_copy($ref);
        return new SaucerUrl(cpy);
    }

    public static SaucerUrl from(@NonNull File file) {
        IntByReference error = new IntByReference(0);
        saucer_url $ref = ntv_url.N.saucer_url_new_from(file.getAbsolutePath(), error);

        if (error.getValue() != 0) {
            throw new IllegalArgumentException("Failed to create SaucerUrl from file, error code: " + error.getValue());
        }

        return new SaucerUrl($ref);
    }

    public static SaucerUrl parse(@NonNull String url) {
        IntByReference error = new IntByReference(0);
        saucer_url $ref = ntv_url.N.saucer_url_new_parse(url, error);

        if (error.getValue() != 0) {
            throw new IllegalArgumentException("Failed to create SaucerUrl from string, error code: " + error.getValue());
        }

        return new SaucerUrl($ref);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public String path() {
        size_t.ByReference sizeRef = new size_t.ByReference();

        // First call to get the size
        ntv_url.N.saucer_url_path($ref, null, sizeRef);

        // Second call to get the actual string
        byte[] buffer = new byte[sizeRef.getValue().intValue()];
        ntv_url.N.saucer_url_path($ref, buffer, sizeRef);

        return new String(buffer, StandardCharsets.UTF_8);
    }

    public String scheme() {
        size_t.ByReference sizeRef = new size_t.ByReference();

        // First call to get the size
        ntv_url.N.saucer_url_scheme($ref, null, sizeRef);

        // Second call to get the actual string
        byte[] buffer = new byte[sizeRef.getValue().intValue()];
        ntv_url.N.saucer_url_scheme($ref, buffer, sizeRef);

        return new String(buffer, StandardCharsets.UTF_8);
    }

    /**
     * @return the host component of the URL, or an empty string if not present.
     */
    public String host() {
        size_t.ByReference sizeRef = new size_t.ByReference();

        // First call to get the size
        ntv_url.N.saucer_url_host($ref, null, sizeRef);

        if (sizeRef.getValue().intValue() == 0) {
            return "";
        }

        // Second call to get the actual string
        byte[] buffer = new byte[sizeRef.getValue().intValue()];
        ntv_url.N.saucer_url_host($ref, buffer, sizeRef);

        return new String(buffer, StandardCharsets.UTF_8);
    }

    /**
     * @return the port component of the URL, or -1 if not present.
     */
    public int port() {
        size_t.ByReference portRef = new size_t.ByReference();
        boolean hasPort = ntv_url.N.saucer_url_port($ref, portRef);

        if (!hasPort) {
            return -1;
        }

        return portRef.getValue().intValue();
    }

    /**
     * @return the user component of the URL, or an empty string if not present.
     */
    public String user() {
        size_t.ByReference sizeRef = new size_t.ByReference();

        // First call to get the size
        ntv_url.N.saucer_url_user($ref, null, sizeRef);

        if (sizeRef.getValue().intValue() == 0) {
            return "";
        }

        // Second call to get the actual string
        byte[] buffer = new byte[sizeRef.getValue().intValue()];
        ntv_url.N.saucer_url_user($ref, buffer, sizeRef);

        return new String(buffer, StandardCharsets.UTF_8);
    }

    /**
     * @return the password component of the URL, or an empty string if not present.
     */
    public String password() {
        size_t.ByReference sizeRef = new size_t.ByReference();

        // First call to get the size
        ntv_url.N.saucer_url_password($ref, null, sizeRef);

        if (sizeRef.getValue().intValue() == 0) {
            return "";
        }

        // Second call to get the actual string
        byte[] buffer = new byte[sizeRef.getValue().intValue()];
        ntv_url.N.saucer_url_password($ref, buffer, sizeRef);

        return new String(buffer, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        size_t.ByReference sizeRef = new size_t.ByReference();

        // First call to get the size
        ntv_url.N.saucer_url_string($ref, null, sizeRef);

        // Second call to get the actual string
        byte[] buffer = new byte[sizeRef.getValue().intValue()];
        ntv_url.N.saucer_url_string($ref, buffer, sizeRef);

        return new String(buffer, StandardCharsets.UTF_8);
    }

}

class SaucerUrlSerializer implements JsonSerializer<SaucerUrl> {

    @Override
    public @Nullable SaucerUrl deserialize(@NonNull JsonElement value, @NonNull Class<?> type, @NonNull Rson rson) throws JsonParseException {
        if (value.isJsonString()) {
            return SaucerUrl.parse(value.getAsString());
        }

        throw new JsonParseException("SaucerUrl must be a string.");
    }

    @Override
    public JsonElement serialize(@NonNull Object v, @NonNull Rson rson) {
        SaucerUrl value = (SaucerUrl) v;
        return new JsonString(value.toString());
    }

}
