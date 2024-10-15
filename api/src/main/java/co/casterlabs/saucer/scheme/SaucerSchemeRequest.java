package co.casterlabs.saucer.scheme;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer._impl.c.SaucerPointerType;
import co.casterlabs.saucer._impl.c._SaucerMemory;
import co.casterlabs.saucer._impl.c.size_t;
import co.casterlabs.saucer.documentation.InternalUseOnly;
import co.casterlabs.saucer.utils.SaucerStash;
import lombok.Getter;

@Getter
@SuppressWarnings("deprecation")
public final class SaucerSchemeRequest extends SaucerPointerType<SaucerSchemeRequest> {
    private static final _Native N = _SaucerNative.load(_Native.class);

    @Deprecated
    @InternalUseOnly
    public SaucerSchemeRequest() {
        // NOOP constructor for JNA.
    }

    @Override
    protected SaucerSchemeRequest newInstanceForJNA() {
        return new SaucerSchemeRequest();
    }

    @Override
    protected void free() {
        // NOOP
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public String method() {
        return N.saucer_scheme_request_method(this);
    }

    public URI uri() {
        String full = N.saucer_scheme_request_url(this);
        return URI.create(full);
    }

    public SaucerStash payload() {
        return N.saucer_scheme_request_content(this);
    }

    public Map<String, String> headers() {
        // Setup the pointers for receiving the value.
        Pointer $$keys = _SaucerMemory.alloc(Native.POINTER_SIZE);
        Pointer $$values = _SaucerMemory.alloc(Native.POINTER_SIZE);
        Pointer $count = _SaucerMemory.alloc(Native.SIZE_T_SIZE);
        N.saucer_scheme_request_headers(this, $$keys, $$values, $count);

        // Get their values & free.
        Pointer $keys = $$keys.getPointer(0);
        Pointer $values = $$values.getPointer(0);
        size_t count = size_t.from($count);
        _SaucerMemory.free($$keys);
        _SaucerMemory.free($$values);
        _SaucerMemory.free($count);

        // Convert the key/values pointers to arrays & free.
        Pointer[] keys = $keys.getPointerArray(0, count.intValue());
        Pointer[] values = $values.getPointerArray(0, count.intValue());
        _SaucerMemory.free($keys);
        _SaucerMemory.free($values);

        // Convert to map.
        Map<String, String> map = new HashMap<>();
        for (int idx = 0; idx < keys.length; idx++) {
            Pointer $key = keys[idx];
            Pointer $value = values[idx];

            map.put($key.getString(0), $value.getString(0));

            // Don't forget to free!
            _SaucerMemory.free($key);
            _SaucerMemory.free($value);
        }
        return map;
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    // https://github.com/saucer/bindings/blob/main/include/saucer/scheme.h
    static interface _Native extends Library {

        String saucer_scheme_request_url(SaucerSchemeRequest instance);

        String saucer_scheme_request_method(SaucerSchemeRequest instance);

        SaucerStash saucer_scheme_request_content(SaucerSchemeRequest instance);

        void saucer_scheme_request_headers(SaucerSchemeRequest instance, Pointer $$keys, Pointer $$values, Pointer $count);

    }

}
