package app.saucer._impl;

import java.io.IOException;

import app.saucer.SaucerBackendType;
import app.saucer._impl._SaucerBackend.FindThisSaucerBackend;

@SuppressWarnings("deprecation")
@FindThisSaucerBackend(-100) // Should be higher priority than Qt5
public class ExperimentalBackendQt6 extends _SaucerBackend {

    @Override
    public boolean checkDependencies() throws IOException {
        return checkForLibraries(
            "Qt6WebEngineCore",
            "Qt6WebEngineWidgets",
            "Qt6WebChannel",
            "Qt6Widgets",
            "Qt6Core"
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
        return SaucerBackendType.QT6;
    }

    @Override
    public String getBuildType() {
        return "Debug";
    }

}
