package co.casterlabs.saucer._impl;

import java.nio.charset.Charset;
import java.util.Collections;

import com.sun.jna.IntegerType;
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
        Resources.loadNatives();
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

    public static class size_t extends IntegerType {
        private static final long serialVersionUID = 1L;

        public size_t() {
            this(0);
        }

        public size_t(long value) {
            super(Native.SIZE_T_SIZE, value, true);
        }

        public static size_t from(Pointer $ptr) {
            long value;
            if (Native.SIZE_T_SIZE == Long.BYTES) {
                value = $ptr.getLong(0);
            } else {
                value = $ptr.getInt(0);
            }
            return new size_t(value);
        }

    }

}
