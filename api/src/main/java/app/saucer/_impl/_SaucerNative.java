package app.saucer._impl;

import java.util.Collections;

import com.sun.jna.Library;
import com.sun.jna.Native;

import app.saucer.documentation.InternalUseOnly;
import lombok.NonNull;

@SuppressWarnings("deprecation")
@Deprecated
@InternalUseOnly
public class _SaucerNative {
    public static String backend;

    public static <T extends Library> T load(@NonNull Class<T> clazz) {
        Resources.loadNatives();
        return Native.load(
            "saucer-bindings",
            clazz,
            Collections.singletonMap(Library.OPTION_STRING_ENCODING, "UTF-8")
        );
    }

}
