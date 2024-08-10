package co.casterlabs.saucer._impl.c;

import java.nio.charset.Charset;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer.documentation.InternalUseOnly;
import lombok.NonNull;

@SuppressWarnings("deprecation")
@Deprecated
@InternalUseOnly
public class _SaucerMemory {
    private static final _Native N = _SaucerNative.load(_Native.class);

    public static void free(@NonNull Pointer $ptr) {
        N.saucer_memory_free($ptr);
    }

    public static Pointer alloc(@NonNull String str) {
        byte[] content = str.getBytes(Charset.defaultCharset());
        Pointer $content = alloc(content.length + 1);
        $content.write(0, content, 0, content.length);
        return $content;
    }

    public static Pointer alloc(int length) {
        Pointer $content = N.saucer_memory_alloc(new size_t(length)); // NON GC'ABLE!
        $content.clear(length);
        return $content;
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    // https://github.com/saucer/bindings/blob/main/include/saucer/memory.h
    static interface _Native extends Library {

        void saucer_memory_free(Pointer $ptr);

        @RequiresFree
        Pointer saucer_memory_alloc(size_t size);

    }

}
