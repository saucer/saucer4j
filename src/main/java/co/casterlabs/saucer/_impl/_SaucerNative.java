package co.casterlabs.saucer._impl;

import java.nio.charset.Charset;
import java.util.Collections;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.documentation.InternalUseOnly;
import lombok.NonNull;

@Deprecated
@InternalUseOnly
public class _SaucerNative {

    static {
        _SaucerNative.class.getClassLoader().setPackageAssertionStatus("co.casterlas.saucer", true);
        // TODO extract natives.
    }

    public static <T extends Library> T load(@NonNull Class<T> clazz) {
        return Native.load(
            "saucer-bindings",
            clazz,
            Collections.singletonMap(Library.OPTION_STRING_ENCODING, "UTF-8")
        );
    }

    public static Pointer allocateUnsafe(@NonNull String str) {
        byte[] content = str.getBytes(Charset.defaultCharset());
        Pointer $content = allocateUnsafe(content.length + 1);
        $content.write(0, content, 0, content.length);
        return $content;
    }

    public static Pointer allocateUnsafe(int length) {
        Pointer $content = new Pointer(Native.malloc(length)); // NON GC'ABLE!
        $content.clear(length);
        return $content;
    }

}
