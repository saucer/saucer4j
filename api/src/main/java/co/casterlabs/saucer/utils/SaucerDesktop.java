package co.casterlabs.saucer.utils;

import com.sun.jna.Library;

import co.casterlabs.saucer._impl._SaucerNative;
import lombok.NonNull;

@SuppressWarnings("deprecation")
public class SaucerDesktop {
    private static final _Native N = _SaucerNative.load(_Native.class);

    public static void open(@NonNull String uri) {
        N.saucer_desktop_open(uri);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    // https://github.com/saucer/bindings/blob/main/include/saucer/desktop.h
    static interface _Native extends Library {

        void saucer_desktop_open(String uri);

    }

}
