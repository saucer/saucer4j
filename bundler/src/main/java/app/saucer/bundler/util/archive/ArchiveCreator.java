package app.saucer.bundler.util.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import co.casterlabs.commons.platform.OSFamily;
import co.casterlabs.commons.platform.Platform;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class ArchiveCreator {
    private static final FastLogger LOGGER = new FastLogger("ArchiveCreator");

    public static void create(Archives.Format format, File inputDir, File destFile) throws FileNotFoundException, IOException {
        switch (format) {
            case ZIP: {
                try (
                    OutputStream fileOut = new FileOutputStream(destFile);
                    ZipArchiveOutputStream out = new ZipArchiveOutputStream(fileOut)) {
                    compress(inputDir, inputDir, out);
                    out.finish();
                }
                return;
            }

            case TAR_GZ: {
                try {
                    if (Platform.osFamily == OSFamily.UNIX) {
                        int exitCode = Runtime.getRuntime().exec(new String[] {
                                "tar",
                                "-czvf",
                                destFile.getAbsolutePath(),
                                "-C",
                                inputDir.getAbsolutePath(),
                                "."
                        })
                            .waitFor();

                        if (exitCode != 0) {
                            fallbackTarBehavior(inputDir, destFile);
                        }
                    } else {
                        fallbackTarBehavior(inputDir, destFile);
                    }
                } catch (InterruptedException e) {
                    throw new IOException(e);
                }

                return;
            }

            default:
                throw new IOException("Unsupported compression format: " + format);
        }
    }

    private static void fallbackTarBehavior(File inputDir, File destFile) throws FileNotFoundException, IOException {
        LOGGER.warn("tar command appears to be unsupported or missing, falling back to java-implementation. THIS WILL DESTROY THE EXECUTABLE BIT.");
        try (
            OutputStream fileOut = new FileOutputStream(destFile);
            OutputStream gzipOut = new GzipCompressorOutputStream(fileOut);
            TarArchiveOutputStream out = new TarArchiveOutputStream(gzipOut)) {
            out.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            compress(inputDir, inputDir, out);
            out.finish();
        }
    }

    private static <E extends ArchiveEntry> void compress(File inputDir, File file, ArchiveOutputStream<E> out) throws IOException, FileNotFoundException {
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                compress(inputDir, sub, out);
            }
            return;
        }

        String entryPath = file.getAbsolutePath().substring(inputDir.getAbsolutePath().length() + 1);
        LOGGER.trace("Compressing: %s", entryPath);

        E entry = out.createArchiveEntry(file, entryPath);
        out.putArchiveEntry(entry);
        try (InputStream in = new FileInputStream(file)) {
            in.transferTo(out);
        }
        out.closeArchiveEntry();
    }

}
