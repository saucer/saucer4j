package app.saucer._impl;

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

import app.saucer.Saucer;
import app.saucer.documentation.InternalUseOnly;
import co.casterlabs.commons.io.streams.StreamUtil;

@SuppressWarnings("deprecation")
@Deprecated
@InternalUseOnly
public abstract class _SaucerBackend {

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

    /**
     * @return usually "Release" or "Debug"
     */
    public abstract String getBuildType();

    public void extractTo(Path targetDir) throws IOException {
        String resourcePath = String.format("natives/%s-%s-%s-%s.zip", Saucer.getSystemTarget(), this.getType(), Saucer.getArchTarget(), this.getBuildType());

        // Grab the zip file from this Jar, extract it to the temp folder above.
        try (InputStream in = Resources.loadResource(resourcePath)) {
            if (in == null) {
                throw new IOException("Could not find native in resources. Expected: " + resourcePath);
            }

            try (ZipInputStream zin = new ZipInputStream(in)) {
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
            } catch (IOException e) {
                throw new IOException("Error when extracting native from resources: " + resourcePath, e);
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
    public static @interface FindThisSaucerBackend {
        /**
         * Priority. For people hacking in their own backends, set your backend priority
         * >= 100.
         */
        int value();

    }

}
