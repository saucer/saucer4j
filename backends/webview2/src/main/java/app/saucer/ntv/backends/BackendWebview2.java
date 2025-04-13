package app.saucer.ntv.backends;

import java.io.IOException;

import app.saucer.ntv.backends.SaucerBackend;
import app.saucer.ntv.backends.SaucerBackendType;
import app.saucer.ntv.backends.SaucerBackend.FindThisSaucerBackend;

@FindThisSaucerBackend(0)
public class BackendWebview2 extends SaucerBackend {

    @Override
    protected boolean checkDependencies() throws IOException {
//        if (!checkForLibraries(
//            "MSVCP140",
//            "VCRUNTIME140"
//        )) {
//            return false;
//        }

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
    protected String[] supportedSystemTargets() {
        return new String[] {
                "Windows"
        };
    }

    @Override
    protected String[] supportedArchTargets() {
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
