package app.saucer._impl;

import java.io.IOException;

import app.saucer.ntv.backend.SaucerBackend;
import app.saucer.ntv.backend.SaucerBackend.FindThisSaucerBackend;
import app.saucer.ntv.backend.SaucerBackendType;

@FindThisSaucerBackend(0)
public class BackendWebkit extends SaucerBackend {

    @Override
    protected boolean checkDependencies() throws IOException {
        return true; // TODO check the macOS version.
    }

    @Override
    protected String[] supportedSystemTargets() {
        return new String[] {
                "macOS"
        };
    }

    @Override
    protected String[] supportedArchTargets() {
        return new String[] {
                "aarch64",
                "x86_64"
        };
    }

    @Override
    public SaucerBackendType getType() {
        return SaucerBackendType.WEBKIT;
    }

    @Override
    public String getBuildType() {
        return "Release";
    }

}
