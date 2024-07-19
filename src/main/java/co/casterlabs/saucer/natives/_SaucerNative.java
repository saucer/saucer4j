package co.casterlabs.saucer.natives;

import java.util.Collections;

import com.sun.jna.Library;
import com.sun.jna.Native;

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

}
