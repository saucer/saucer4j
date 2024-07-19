package co.casterlabs.saucer.natives;

import java.nio.charset.Charset;
import java.util.Collections;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.documentation.InternalUseOnly;

@Deprecated
@InternalUseOnly
public class _SaucerNative {

    static {
        _SaucerNative.class.getClassLoader().setPackageAssertionStatus("co.casterlas.saucer", true);
        // TODO extract natives.
    }

    public static <T extends Library> T load(Class<T> clazz) {
        return Native.load(
            "saucer-bindings",
            clazz,
            Collections.singletonMap(Library.OPTION_STRING_ENCODING, "UTF-8")
        );
    }

    public static Pointer allocString(String str) {
        return allocString(str, Charset.defaultCharset());
    }

    public static Pointer allocString(String str, Charset charset) {
        byte[] content = str.getBytes(charset);

        Pointer $content = new Pointer(Native.malloc(content.length + 1)); // NON GC'ABLE!
        $content.clear(content.length + 1);
        $content.write(0, content, 0, content.length);
        return $content;
    }

}
