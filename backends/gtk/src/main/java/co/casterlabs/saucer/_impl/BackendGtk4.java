package co.casterlabs.saucer._impl;

import java.io.IOException;

import co.casterlabs.saucer._impl._SaucerBackend.FindThisSaucerBackend;

@SuppressWarnings("deprecation")
@FindThisSaucerBackend(2) // Should be higher than Qt6
public class BackendGtk4 extends _SaucerBackend {

    @Override
    public boolean checkDependencies() throws IOException {
        return checkForLibraries(
            "libgtk-4",
            "libwebkitgtk-6.0",
            "libadwaita-1"
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
                "x86_64",
                "aarch64",
                "arm",
                "ppc64le"
        };
    }

    @Override
    public String getType() {
        return "Gtk4";
    }

    @Override
    public String getBuildType() {
        return "Release";
    }

}
