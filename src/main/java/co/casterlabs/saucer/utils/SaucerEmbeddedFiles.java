package co.casterlabs.saucer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import lombok.SneakyThrows;

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
        Class<?> caller = getCallerClassName();

        String fullPath = basePath + filename;
        if (fullPath.startsWith("/")) {
            // Strip leading slashes.
            fullPath = basePath.substring(1);
        }

        InputStream in = caller.getResourceAsStream(fullPath);
        if (in == null) {
            // Some IDEs mangle the resource location when launching directly. Let's try
            // that as a backup.
            in = caller.getResourceAsStream("/" + fullPath);
        }
        if (in == null) {
            // Another mangle.
            in = caller.getResourceAsStream("/resources/" + fullPath);
        }

        assert in != null : "Could not locate resource: " + fullPath;

        this.add(filename, mime, in);
        return this;
    }

    @SneakyThrows
    public SaucerEmbeddedFiles scanForResources(@NonNull String basePath) {
        Class<?> caller = getCallerClassName();
        File location = Paths.get(caller.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile();

        if (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }

        if (location.isDirectory()) {
            // Some IDE environments don't build a full jar, they just compile everything
            // and then place a folder on the classpath. We handle that here.
            Path joinedPath = Path.of(location.getAbsolutePath(), basePath);
            List<Path> discovered = listAllInDirectory(joinedPath);

            for (Path path : discovered) {
                String relativePath = path.toAbsolutePath().toString()
                    .substring(joinedPath.toString().length() + 1) // the + 1 is a leading '/'
                    .replace('\\', '/'); // Windows fix
                String mimeType = MimeTypes.getMimeForFile(relativePath);

                this.add(relativePath, mimeType, Files.readAllBytes(path));
            }
            return this;
        }

        String locationExtension = MimeTypes.getFileExtension(location.toString());
        switch (locationExtension) {
            case "jar":
            case "war":
            case "ear":
                try (FileInputStream fis = new FileInputStream(location); ZipInputStream zipIn = new ZipInputStream(fis)) {
                    for (ZipEntry ze; (ze = zipIn.getNextEntry()) != null;) {
                        if (ze.isDirectory()) continue; // We don't need to create directories.
                        String entryName = ze.getName();

                        String relativePath; // Note that we don't have to worry about zip-slip.
                        if (entryName.startsWith(basePath)) {
                            relativePath = entryName.substring(basePath.length() + 1); // the + 1 is a leading '/'
                        } else if (entryName.startsWith("resources/" + basePath)) {
                            // Some IDEs will mangle the resources and place them under a sub-folder called
                            // `resources/`.
                            relativePath = entryName.substring("resources/".length() + basePath.length() + 1); // the +1 is a leading /
                        } else {
                            continue;
                        }

                        String mimeType = MimeTypes.getMimeForFile(relativePath);
                        this.add(relativePath, mimeType, zipIn);
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Saucer cannot load " + locationExtension + " files yet. Please file an issue on GitHub and we'll add support if it's possible :)");
        }

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

    @SneakyThrows
    private static Class<?> getCallerClassName() {
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            String className = ste
                .getClassName()
                .split("\\$")[0]; // Ignore inner classes.

            if (!className.equals(SaucerEmbeddedFiles.class.getName()) && !className.contains("java.lang.Thread")) {
                return Class.forName(className);
            }
        }
        return null;
    }

    private static List<Path> listAllInDirectory(Path directory) throws IOException {
        List<Path> fileList = new ArrayList<>();
        Files.walkFileTree(directory, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile()) {
                    fileList.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return fileList;
    }

}
