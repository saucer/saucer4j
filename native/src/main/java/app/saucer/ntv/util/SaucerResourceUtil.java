package app.saucer.ntv.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.streams.StreamUtil;
import lombok.SneakyThrows;

public class SaucerResourceUtil {

    @SneakyThrows
    public static String loadResourceString(String name) {
        return StreamUtil.toString(loadResource(name), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public static InputStream loadResource(String name) {
        String fullPath = "app/saucer/_impl/" + name;

        InputStream in = SaucerResourceUtil.class.getResourceAsStream(fullPath);
        if (in == null) {
            // Some IDEs mangle the resource location when launching directly. Let's try
            // that as a backup.
            in = SaucerResourceUtil.class.getResourceAsStream("/" + fullPath);
        }
        if (in == null) {
            // Another mangle.
            in = SaucerResourceUtil.class.getResourceAsStream("/resources/" + fullPath);
        }

        assert in != null : "Could not locate internal resource: " + fullPath;

        return in;
    }

}
