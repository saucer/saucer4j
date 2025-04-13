package app.saucer.ntv.backends;

import java.io.IOException;

import app.saucer.ntv.backends.SaucerBackend;
import app.saucer.ntv.backends.SaucerBackendType;
import app.saucer.ntv.backends.SaucerBackend.FindThisSaucerBackend;

@FindThisSaucerBackend(-101)
public class ExperimentalBackendQt5 extends SaucerBackend {

    @Override
    protected boolean checkDependencies() throws IOException {
        return checkForLibraries(
            "Qt5WebEngineCore",
            "Qt5WebEngineWidgets",
            "Qt5WebChannel",
            "Qt5Widgets",
            "Qt5Core"
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
        return SaucerBackendType.QT5;
    }

    @Override
    public String getBuildType() {
        return "Debug";
    }

}
