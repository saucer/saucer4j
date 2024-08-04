package co.casterlabs.saucer._impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import com.sun.jna.Library;
import com.sun.jna.Native;

import co.casterlabs.commons.io.streams.StreamUtil;

public abstract class Backend {

    public abstract boolean canLoad() throws IOException;

    public abstract String[] supportedSystemTargets();

    public abstract String[] supportedArchTargets();

    public abstract String getType();

    public abstract void extractTo(Path targetDir) throws IOException;

    protected static String exec(String... command) throws IOException {
        return StreamUtil.toString(
            Runtime.getRuntime().exec(command).getInputStream(),
            Charset.defaultCharset()
        );
    }

    protected static boolean checkForLibraries(String... libs) {
        try {
            for (String lib : libs) {
                Native.load(lib, DummyLibrary.class);
            }
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    static interface DummyLibrary extends Library {

    }

}
