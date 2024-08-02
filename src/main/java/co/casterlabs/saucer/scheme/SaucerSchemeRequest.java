package co.casterlabs.saucer.scheme;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import co.casterlabs.saucer._impl._SafePointer;
import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer._impl._SaucerNative.size_t;
import co.casterlabs.saucer.documentation.InternalUseOnly;
import co.casterlabs.saucer.utils.SaucerStash;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("deprecation")
public final class SaucerSchemeRequest {
    private static final _Native N = _SaucerNative.load(_Native.class);

    private Pointer $ptr;

    @Deprecated
    @InternalUseOnly
    public SaucerSchemeRequest(@NonNull Pointer pointer) {
        this.$ptr = pointer; // free()'d for us.
    }

    public String method() {
        _SafePointer $method = N.saucer_request_method($ptr);
        return $method.p().getString(0);
    }

    public String url() {
        _SafePointer $full = N.saucer_request_url($ptr);
        String full = $full.p().getString(0);
        return full.substring(full.indexOf(':') + 1); // "saucer:/index.html" -> "/index.html"
    }

    public SaucerStash payload() {
        Pointer $stash = N.saucer_request_content($ptr);
        return new SaucerStash($stash);
    }

    public Map<String, String> headers() {
        // Note that `_SafePointer` will automatically get free()'d for us.
        // But we still have to manually free the regular Pointers.
        _SafePointer $$keys = _SafePointer.allocate(Native.POINTER_SIZE);
        _SafePointer $$values = _SafePointer.allocate(Native.POINTER_SIZE);
        _SafePointer $count = _SafePointer.allocate(Native.SIZE_T_SIZE);
        N.saucer_request_headers($ptr, $$keys, $$values, $count);

        Pointer $keys = $$keys.p().getPointer(0);
        Pointer $values = $$values.p().getPointer(0);
        size_t count = size_t.from($count.p());

        Pointer[] keys = $keys.getPointerArray(0, count.intValue());
        Pointer[] values = $values.getPointerArray(0, count.intValue());

        _SaucerNative.free($keys);
        _SaucerNative.free($values);

        // Convert to mapping.
        Map<String, String> map = new HashMap<>();
        for (int idx = 0; idx < keys.length; idx++) {
            Pointer $key = keys[idx];
            Pointer $value = values[idx];

            map.put($key.getString(0), $value.getString(0));
            _SaucerNative.free($key);
            _SaucerNative.free($value);
        }
        return map;
    }

    // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/scheme.h
    static interface _Native extends Library {

        _SafePointer saucer_request_url(Pointer $instance);

        _SafePointer saucer_request_method(Pointer $instance);

        /* already free()'d */ Pointer saucer_request_content(Pointer $instance);

        void saucer_request_headers(Pointer $instance, _SafePointer $$keys, _SafePointer $$values, _SafePointer $count);

    }

}
