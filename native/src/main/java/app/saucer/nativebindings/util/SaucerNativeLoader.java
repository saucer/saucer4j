package app.saucer.nativebindings.util;

import com.sun.jna.Library;
import com.sun.jna.Native;

import lombok.NonNull;

public class SaucerNativeLoader {
    private static DummyLibrary mainSaucerLib = null;

    public static <T extends Library> T load(@NonNull Class<T> clazz) {
        if (mainSaucerLib == null) {
            mainSaucerLib = Native.load("saucer", DummyLibrary.class);
        }

        return Native.load(
            "saucer-bindings",
            clazz
        );
    }

    public static interface DummyLibrary extends Library {
    }

}
