package app.saucer.ntv.backends;

import java.io.IOException;

import app.saucer.ntv.backends.SaucerBackend;
import app.saucer.ntv.backends.SaucerBackendType;
import app.saucer.ntv.backends.SaucerBackend.FindThisSaucerBackend;

@FindThisSaucerBackend(-100) // Should be higher priority than Qt5
public class ExperimentalBackendQt6 extends SaucerBackend {

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
                "aarch64"
        };
    }

    @Override
    public SaucerBackendType getType() {
        return SaucerBackendType.QT6;
    }

    @Override
    public String getBuildType() {
        return "Debug";
    }

}
