package co.casterlabs.saucer.scheme;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer.documentation.InternalUseOnly;
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
        return N.saucer_request_method($ptr);
    }

    public String url() {
        String full = N.saucer_request_url($ptr);
        return full.substring(full.indexOf(':') + 1); // "saucer:/index.html" -> "/index.html"
    }

    public SaucerStash payload() {
        Pointer $stash = N.saucer_request_content($ptr);
        return new SaucerStash($stash);
    }

//    public Map<String, String> headers() {
//        Map<String, String> map = new HashMap<>();
//
//        Pointer $keys;
//        Pointer $values;
//        size_t count;
//
//        {
//            Pointer $$keys = _SaucerNative.allocateUnsafe(Native.POINTER_SIZE);
//            Pointer $$values = _SaucerNative.allocateUnsafe(Native.POINTER_SIZE);
//            Pointer $count = _SaucerNative.allocateUnsafe(Native.SIZE_T_SIZE);
//            N.saucer_request_headers($ptr, $$keys, $$values, $count);
//
//            $keys = $$keys.getPointer(0);
//            $values = $$values.getPointer(0);
//            count = size_t.from($count);
//        }
//
//        return map;
//    }

    // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/scheme.h
    static interface _Native extends Library {

        String saucer_request_url(Pointer $instance);

        String saucer_request_method(Pointer $instance);

        Pointer saucer_request_content(Pointer $instance);

//        void saucer_request_headers(Pointer $instance, Pointer $$keys, Pointer $$values, Pointer $count);

    }

}
