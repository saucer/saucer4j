package co.casterlabs.saucer._impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import co.casterlabs.saucer.Saucer;

public class BackendWebview2 extends Backend {

    @Override
    public boolean canLoad() throws IOException {
        // https://learn.microsoft.com/en-us/microsoft-edge/webview2/concepts/distribution?tabs=dotnetcsharp#detect-if-a-webview2-runtime-is-already-installed

        if (regKeyPresent("HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\Microsoft\\EdgeUpdate\\Clients\\{F3017226-FE2A-4295-8BDF-00C3A9A7E4C5}")) {
            return true;
        }
        if (regKeyPresent("HKEY_CURRENT_USER\\Software\\Microsoft\\EdgeUpdate\\Clients\\{F3017226-FE2A-4295-8BDF-00C3A9A7E4C5}")) {
            return true;
        }

        return false;
    }

    @Override
    public String[] supportedSystemTargets() {
        return new String[] {
                "WINDOWS_NT"
        };
    }

    @Override
    public String[] supportedArchTargets() {
        return new String[] {
                "x86_64"
        };
    }

    @Override
    public String getType() {
        return "WebView2";
    }

    @Override
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

    private static boolean regKeyPresent(String key) throws IOException {
        return !exec("reg", "query", key, "/v", "pv").contains("ERROR: ");
    }

}
