package app.saucer.ntv.backends;

import java.io.IOException;

import app.saucer.ntv.backends.SaucerBackend;
import app.saucer.ntv.backends.SaucerBackendType;
import app.saucer.ntv.backends.SaucerBackend.FindThisSaucerBackend;

@FindThisSaucerBackend(0)
public class BackendQt5 extends SaucerBackend {

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
                "x86_64"
        };
    }

    @Override
    public SaucerBackendType getType() {
        return SaucerBackendType.QT5;
    }

    @Override
    public String getBuildType() {
        return "Release";
    }

}
