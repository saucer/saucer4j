package app.saucer.ntv.backends;

import java.io.IOException;

import app.saucer.ntv.backends.SaucerBackend.FindThisSaucerBackend;

@FindThisSaucerBackend(1) // Should be higher priority than Qt5
public class BackendQt6 extends SaucerBackend {

    @Override
    protected boolean checkDependencies() throws IOException {
        return checkForLibraries(
            "Qt6WebEngineCore",
            "Qt6WebEngineWidgets",
            "Qt6WebChannel",
            "Qt6Widgets",
            "Qt6Core"
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
                "x86",
                "x86_64",
//                "aarch64" // TODO investigate build errors.
        };
    }

    @Override
    public SaucerBackendType getType() {
        return SaucerBackendType.QT6;
    }

    @Override
    public String getBuildType() {
        return "Release";
    }

}
