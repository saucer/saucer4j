package app.saucer._impl;

import java.io.IOException;

import app.saucer.SaucerBackendType;
import app.saucer._impl._SaucerBackend.FindThisSaucerBackend;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;

@SuppressWarnings("deprecation")
@FindThisSaucerBackend(0)
public class BackendWebview2 extends _SaucerBackend {

    @Override
    public boolean checkDependencies() throws IOException {
        if (Platform.osDistribution != OSDistribution.WINDOWS_NT) {
            return false;
        }

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
                "Windows"
        };
    }

    @Override
    public String[] supportedArchTargets() {
        return new String[] {
                "x86_64"
        };
    }

    @Override
    public SaucerBackendType getType() {
        return SaucerBackendType.WEBVIEW2;
    }

    @Override
    public String getBuildType() {
        return "Release";
    }

    private static boolean regKeyPresent(String key) {
        try {
            return !exec("reg", "query", key, "/v", "pv").contains("ERROR: ");
        } catch (IOException ignored) {
            return false;
        }
    }

}
