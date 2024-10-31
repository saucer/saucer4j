package app.saucer._impl;

import java.io.IOException;

import app.saucer.SaucerBackendType;
import app.saucer._impl._SaucerBackend.FindThisSaucerBackend;

@SuppressWarnings("deprecation")
@FindThisSaucerBackend(-101)
public class ExperimentalBackendQt5 extends _SaucerBackend {

    @Override
    public boolean checkDependencies() throws IOException {
        return checkForLibraries(
            "Qt5WebEngineCore",
            "Qt5WebEngineWidgets",
            "Qt5WebChannel",
            "Qt5Widgets",
            "Qt5Core"
        );
    }

    @Override
    public String[] supportedSystemTargets() {
        return new String[] {
                "GNU_Linux"
        };
    }

    @Override
    public String[] supportedArchTargets() {
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
