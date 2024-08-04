package co.casterlabs.saucer._impl;

import java.io.IOException;

public class BackendQt5 extends Backend {

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
                "aarch64",
                "mips64el",
                "x86_64"
        };
    }

    @Override
    public String getType() {
        return "Qt5";
    }

}
