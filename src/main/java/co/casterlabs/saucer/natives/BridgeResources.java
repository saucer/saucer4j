package co.casterlabs.saucer.natives;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.saucer.utils.SaucerEmbeddedFiles;
import lombok.SneakyThrows;

class BridgeResources {

    static String ipc_object_fmt = load("ipc_object_fmt.js");
    static String init = load("init.js");

    @SneakyThrows
    private static String load(String name) {
        String fullPath = "co/casterlabs/saucer/natives/bridge/" + name;

        InputStream in = SaucerEmbeddedFiles.class.getResourceAsStream(fullPath);
        if (in == null) {
            // Some IDEs mangle the resource location when launching directly. Let's try
            // that as a backup.
            in = SaucerEmbeddedFiles.class.getResourceAsStream("/" + fullPath);
        }
        if (in == null) {
            // Another mangle.
            in = SaucerEmbeddedFiles.class.getResourceAsStream("/resources/" + fullPath);
        }

        assert in != null : "Could not locate internal resource: " + fullPath;

        return StreamUtil.toString(in, StandardCharsets.UTF_8);
    }

}
