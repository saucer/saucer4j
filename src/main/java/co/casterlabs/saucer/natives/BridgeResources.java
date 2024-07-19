package co.casterlabs.saucer.natives;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.streams.StreamUtil;

class BridgeResources {

    static String ipc_object_fmt = "";
    static {
        try {
            ipc_object_fmt = StreamUtil.toString(BridgeResources.class.getResourceAsStream("/co/casterlabs/saucer/bridge/ipc_object_fmt.js"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String init = "";
    static {
        try {
            init = StreamUtil.toString(BridgeResources.class.getResourceAsStream("/co/casterlabs/saucer/bridge/init.js"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
