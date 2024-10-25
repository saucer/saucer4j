package app.saucer.bundler.util.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class ArchiveExtractor {
    private static final FastLogger LOGGER = new FastLogger("ArchiveExtractor");

    @SuppressWarnings("deprecation")
    public static void extract(Archives.Format format, File archiveFile, File destDir) throws FileNotFoundException, IOException {
        destDir.mkdirs();

        LOGGER.info("Extracting %s to %s.", archiveFile, destDir);
        switch (format) {
            // These are not seekable and thus use a stream implementation.

            case TAR_GZ:
                try (
                    FileInputStream fin = new FileInputStream(archiveFile);
                    GzipCompressorInputStream gzin = new GzipCompressorInputStream(fin);
                    TarArchiveInputStream ain = new TarArchiveInputStream(gzin)) {
                    ArchiveEntry entry = null;
                    while ((entry = ain.getNextEntry()) != null) {
                        File newFile = shouldExtract(destDir, entry);
                        if (newFile == null) continue;

                        extract(newFile, ain);
                    }
                }
                break;

            case TAR_XZ:
                try (
                    FileInputStream fin = new FileInputStream(archiveFile);
                    XZCompressorInputStream xzin = new XZCompressorInputStream(fin);
                    TarArchiveInputStream ain = new TarArchiveInputStream(xzin)) {
                    ArchiveEntry entry = null;
                    while ((entry = ain.getNextEntry()) != null) {
                        File newFile = shouldExtract(destDir, entry);
                        if (newFile == null) continue;

                        extract(newFile, ain);
                    }
                }
                break;

            case TAR:
                try (
                    FileInputStream fin = new FileInputStream(archiveFile);
                    TarArchiveInputStream ain = new TarArchiveInputStream(fin)) {
                    ArchiveEntry entry = null;
                    while ((entry = ain.getNextEntry()) != null) {
                        File newFile = shouldExtract(destDir, entry);
                        if (newFile == null) continue;

                        extract(newFile, ain);
                    }
                }
                break;

            // These are seekable.

            case SEVENZIP:
                try (SevenZFile archive = new SevenZFile(archiveFile)) {
                    for (SevenZArchiveEntry entry : archive.getEntries()) {
                        File newFile = shouldExtract(destDir, entry);
                        if (newFile == null) continue;

                        try (InputStream in = archive.getInputStream(entry)) {
                            extract(newFile, in);
                        }
                    }
                }
                break;

            case ZIP:
                try (ZipFile archive = new ZipFile(archiveFile)) {
                    for (ZipArchiveEntry entry : Collections.list(archive.getEntries())) {
                        File newFile = shouldExtract(destDir, entry);
                        if (newFile == null) continue;

                        try (InputStream in = archive.getInputStream(entry)) {
                            extract(newFile, in);
                        }
                    }
                }
                break;
        }

        LOGGER.info("Done.");
    }

    /**
     * @return null, if you should NOT extract.
     */
    private static File shouldExtract(File dest, ArchiveEntry zipEntry) throws FileNotFoundException, IOException {
        // We ignore directories.
        if (zipEntry.isDirectory()) {
            return null;
        }

        String filename = zipEntry.getName();
        LOGGER.trace("Found file in archive: %s", filename);

        File newFile = newFileNoSlip(dest, filename);
        newFile.getParentFile().mkdirs(); // Create the parent directory.

        return newFile;
    }

    private static void extract(File newFile, InputStream in) throws FileNotFoundException, IOException {
        LOGGER.trace("    Extracting file: %s", newFile);

        // Extract the file.
        try (FileOutputStream out = new FileOutputStream(newFile)) {
            in.transferTo(out);
        }

        LOGGER.trace("    Wrote file to: %s", newFile);
    }

    private static File newFileNoSlip(File destDir, String filename) throws IOException {
        File destFile = new File(destDir, filename);

        String destDirPath = destDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            String message = "    File was outside of the destination directory. (ZipSlip)";
            LOGGER.fatal(message);
            throw new IOException(message);
        }

        return destFile;
    }

}
