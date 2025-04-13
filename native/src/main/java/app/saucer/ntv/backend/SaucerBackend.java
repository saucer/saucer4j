package app.saucer.ntv.backend;

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

import com.sun.jna.Native;

import app.saucer.ntv.util.DummyLibrary;
import app.saucer.ntv.util.SaucerResourceUtil;
import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.commons.platform.LinuxLibC;
import co.casterlabs.commons.platform.Platform;
import lombok.SneakyThrows;

public abstract class SaucerBackend {

    public static String getArchTarget() {
        return Platform.archTarget;
    }

    @SneakyThrows
    public static String getSystemTarget() {
        switch (Platform.osDistribution) {
            case LINUX:
                if (LinuxLibC.isGNU()) {
                    return "GNU_Linux";
                } else {
                    return "MUSL_Linux";
                }

            case WINDOWS_NT:
                return "Windows";

            case MACOS:
                return "macOS";

            default:
                return null;
        }
    }

    public final boolean canLoad() throws IOException {
        if (Arrays.binarySearch(this.supportedSystemTargets(), getSystemTarget()) == -1) {
            return false;
        }
        if (Arrays.binarySearch(this.supportedArchTargets(), getArchTarget()) == -1) {
            return false;
        }

        return this.checkDependencies();
    }

    protected abstract boolean checkDependencies() throws IOException;

    protected abstract String[] supportedSystemTargets();

    protected abstract String[] supportedArchTargets();

    public abstract SaucerBackendType getType();

    /**
     * @return usually "Release" or "Debug"
     */
    public abstract String getBuildType();

    public void extractTo(Path targetDir) throws IOException {
        String resourcePath = String.format("natives/%s-%s-%s-%s.zip", getSystemTarget(), this.getType().toString(), getArchTarget(), this.getBuildType());

        // Grab the zip file from this Jar, extract it to the temp folder above.
        try (InputStream in = SaucerResourceUtil.loadResource(resourcePath)) {
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
