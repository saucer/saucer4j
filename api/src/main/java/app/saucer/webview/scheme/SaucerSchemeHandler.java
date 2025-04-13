package app.saucer.webview.scheme;

import java.io.InputStream;
import java.net.URI;

import co.casterlabs.commons.io.streams.StreamUtil;
import lombok.NonNull;

@FunctionalInterface
public interface SaucerSchemeHandler {

    public SaucerSchemeResponse handle(SaucerSchemeRequest request) throws Throwable;

    public static SaucerSchemeHandler fromResources(@NonNull Class<?> clazz) {
        return fromResources(clazz, "");
    }

    public static SaucerSchemeHandler fromResources(@NonNull Class<?> clazz, @NonNull String basePath) {
        return (SaucerSchemeRequest request) -> {
            String fullPath = basePath + URI.create(request.url()).getPath(); // /path/whatever.html

            InputStream in = clazz.getResourceAsStream(fullPath);
            if (in == null) {
                // Some IDEs mangle the resource location when launching directly. Let's try
                // that as a backup.
                in = clazz.getResourceAsStream(fullPath.substring(1)); // /path/whatever.html -> path/whatever.html
            }
            if (in == null) {
                // Another mangle.
                in = clazz.getResourceAsStream("/resources" + fullPath); // /path/whatever.html -> /resources/path/whatever.html
            }

            if (in == null) {
                return null;
            }

            byte[] data = StreamUtil.toBytes(in);
            String mime = MimeTypes.getMimeForFile(fullPath);
            return SaucerSchemeResponse.create(data, mime);
        };
    }

}
