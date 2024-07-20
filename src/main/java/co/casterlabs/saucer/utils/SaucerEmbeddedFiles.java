package co.casterlabs.saucer.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.sun.jna.IntegerType;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.saucer._impl._SafePointer;
import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer.documentation.PointerType;
import co.casterlabs.saucer.utils.SaucerEmbeddedFiles._Native.size_t;
import lombok.Getter;
import lombok.NonNull;

@Getter
@PointerType
@SuppressWarnings("deprecation")
public final class SaucerEmbeddedFiles extends _SafePointer {
    private static final _Native N = _SaucerNative.load(_Native.class);

    public SaucerEmbeddedFiles() {
        super.setup(N.saucer_embedded_files_new(), N::saucer_embedded_files_free);
    }

    public SaucerEmbeddedFiles add(@NonNull String filename, @NonNull String mime, @NonNull byte[] content) {
        Pointer $content = new Pointer(Native.malloc(content.length)); // NON GC'ABLE!
        $content.write(0, content, 0, content.length);

        if (mime.toLowerCase().startsWith("text/") && !mime.toLowerCase().contains("charset=")) {
            mime += "; charset=utf-8"; // DON'T BREAK THE WEB!
        }

        N.saucer_embedded_files_add(this.p(), filename, mime, $content, new size_t(content.length));
        return this;
    }

    public SaucerEmbeddedFiles add(@NonNull String filename, @NonNull String mime, @NonNull InputStream content) throws IOException {
        this.add(filename, mime, StreamUtil.toBytes(content));
        return this;
    }

    public SaucerEmbeddedFiles add(@NonNull String filename, @NonNull String mime, @NonNull String content) {
        this.add(filename, mime, content.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public SaucerEmbeddedFiles addResource(@NonNull String filename, @NonNull String mime) throws IOException {
        return this.addResource(filename, mime, "");
    }

    public SaucerEmbeddedFiles addResource(@NonNull String basePath, @NonNull String filename, @NonNull String mime) throws IOException {
        String fullPath = basePath + filename;
        if (fullPath.startsWith("/")) {
            // Strip leading slashes.
            fullPath = basePath.substring(1);
        }

        InputStream in = SaucerEmbeddedFiles.class.getResourceAsStream(fullPath);
        if (in == null) {
            // Some IDEs mangle the resource location when launching directly. Let's try
            // that as a backup.
            in = SaucerEmbeddedFiles.class.getResourceAsStream("/" + fullPath);
        }
        if (in == null) {
            // Another mangle.
            in = SaucerEmbeddedFiles.class.getResourceAsStream("/resources/" + fullPath);
        }

        assert in != null : "Could not locate resource: " + fullPath;

        this.add(filename, mime, in);
        return this;
    }

    // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/embed.h
    static interface _Native extends Library {

        Pointer saucer_embedded_files_new();

        void saucer_embedded_files_free(Pointer $instance);

        void saucer_embedded_files_add(Pointer $instance, String filename, String mime, Pointer content, size_t length);

        static class size_t extends IntegerType {
            public static final size_t ZERO = new size_t();

            private static final long serialVersionUID = 1L;

            public size_t() {
                this(0);
            }

            public size_t(long value) {
                super(Native.SIZE_T_SIZE, value, true);
            }
        }

    }

}
