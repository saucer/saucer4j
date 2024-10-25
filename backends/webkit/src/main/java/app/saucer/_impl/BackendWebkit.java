package app.saucer._impl;

import java.io.IOException;

import app.saucer._impl._SaucerBackend;
import app.saucer._impl._SaucerBackend.FindThisSaucerBackend;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;

@SuppressWarnings("deprecation")
@FindThisSaucerBackend(0)
public class BackendWebkit extends _SaucerBackend {

    @Override
    public boolean checkDependencies() throws IOException {
        return Platform.osDistribution == OSDistribution.MACOS;
    }

    @Override
    public String[] supportedSystemTargets() {
        return new String[] {
                "macOS"
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
        return "WebKit";
    }

    @Override
    public String getBuildType() {
        return "Release";
    }

}
