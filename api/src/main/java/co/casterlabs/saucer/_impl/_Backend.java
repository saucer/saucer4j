package co.casterlabs.saucer._impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sun.jna.Library;
import com.sun.jna.Native;

import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.saucer.Saucer;

public abstract class _Backend {

    public boolean canLoad() throws IOException {
        if (Arrays.binarySearch(this.supportedSystemTargets(), Saucer.getSystemTarget()) == -1) {
            return false;
        }
        if (Arrays.binarySearch(this.supportedArchTargets(), Saucer.getArchTarget()) == -1) {
            return false;
        }

        return this.checkDependencies();
    }

    protected abstract boolean checkDependencies() throws IOException;

    public abstract String[] supportedSystemTargets();

    public abstract String[] supportedArchTargets();

    public abstract String getType();

    public void extractTo(Path targetDir) throws IOException {
        String resourcePath = String.format("natives/%s-%s-%s-Release.zip", Saucer.getSystemTarget(), this.getType(), Saucer.getArchTarget());

        // Grab the zip file from this Jar, extract it to the temp folder above.
        try (InputStream in = Resources.loadResource(resourcePath); ZipInputStream zin = new ZipInputStream(in)) {
            for (ZipEntry ze; (ze = zin.getNextEntry()) != null;) {
                Path resolvedPath = targetDir.resolve(ze.getName()).normalize();
                if (!resolvedPath.startsWith(targetDir)) {
                    // https://snyk.io/research/zip-slip-vulnerability
                    throw new RuntimeException("Attempted zip-slip!" + ze.getName());
                }
                if (ze.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    Files.createDirectories(resolvedPath.getParent());
                    Files.copy(zin, resolvedPath);
                }
            }
        }
    }

    protected static String exec(String... command) throws IOException {
        return StreamUtil.toString(
            Runtime.getRuntime().exec(command).getInputStream(),
            Charset.defaultCharset()
        );
    }

    protected static boolean checkForLibraries(String... libs) {
        try {
            for (String lib : libs) {
                Native.load(lib, DummyLibrary.class);
            }
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    static interface DummyLibrary extends Library {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface SaucerBackend {

    }

}
