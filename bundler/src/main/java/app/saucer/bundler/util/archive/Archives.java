package app.saucer.bundler.util.archive;

import java.io.File;

import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;

public class Archives {

    public static @Nullable Format probeFormat(File file) {
        String pathname = file
            .getPath()
            .toLowerCase();

        for (Format f : Format.values()) {
            if (pathname.endsWith(f.extension)) {
                return f;
            }
        }

        return null;
    }

    @AllArgsConstructor
    public static enum Format {
        // @formatter:off
        TAR_GZ   (".tar.gz"),
        TAR_XZ   (".tar.xz"),
        TAR      (".tar"),
        SEVENZIP (".7z"),
        ZIP      (".zip"),
        ;
        // @formatter:on

        public final String extension;

    }

}
