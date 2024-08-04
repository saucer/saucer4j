package co.casterlabs.saucer._impl;

import java.io.IOException;

public class BackendQt6 extends Backend {

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
                "aarch64",
                "x86_64"
        };
    }

    @Override
    public String getType() {
        return "Qt6";
    }

}