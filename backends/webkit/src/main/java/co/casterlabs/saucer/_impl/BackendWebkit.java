package co.casterlabs.saucer._impl;

import java.io.IOException;

import co.casterlabs.saucer._impl._SaucerBackend.FindThisSaucerBackend;

@SuppressWarnings("deprecation")
@FindThisSaucerBackend(0)
public class BackendWebkit extends _SaucerBackend {

    @Override
    public boolean checkDependencies() throws IOException {
        return true;
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
                "aarch64"
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
