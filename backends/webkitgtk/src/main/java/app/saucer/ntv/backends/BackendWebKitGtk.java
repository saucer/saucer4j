package app.saucer.ntv.backends;

import java.io.IOException;

import app.saucer.ntv.backends.SaucerBackend;
import app.saucer.ntv.backends.SaucerBackendType;
import app.saucer.ntv.backends.SaucerBackend.FindThisSaucerBackend;

@FindThisSaucerBackend(2) // Should be higher than Qt6
public class BackendWebKitGtk extends SaucerBackend {

    @Override
    protected boolean checkDependencies() throws IOException {
        return checkForLibraries(
            "libgtk-4",
            "libwebkitgtk-6.0",
            "libadwaita-1"
        );
    }

    @Override
    protected String[] supportedSystemTargets() {
        return new String[] {
                "GNU_Linux"
        };
    }

    @Override
    protected String[] supportedArchTargets() {
        return new String[] {
                "x86_64",
                "aarch64",
                "arm",
                "ppc64le"
        };
    }

    @Override
    public SaucerBackendType getType() {
        return SaucerBackendType.WEBKITGTK;
    }

    @Override
    public String getBuildType() {
        return "Release";
    }

}
