package co.casterlabs.saucer.scheme;

import java.io.InputStream;

import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.saucer.scheme.SaucerSchemeResponse.SaucerRequestError;
import co.casterlabs.saucer.utils.MimeTypes;
import co.casterlabs.saucer.utils.SaucerStash;
import lombok.NonNull;

@FunctionalInterface
public interface SaucerSchemeHandler {

    public SaucerSchemeResponse handle(SaucerSchemeRequest request) throws Throwable;

    public static SaucerSchemeHandler fromResources(@NonNull Class<?> clazz) {
        return fromResources(clazz, "");
    }

    public static SaucerSchemeHandler fromResources(@NonNull Class<?> clazz, @NonNull String basePath) {
        return (SaucerSchemeRequest request) -> {
            String fullPath = basePath + request.url();

            InputStream in = clazz.getResourceAsStream(fullPath);
            if (in == null) {
                // Some IDEs mangle the resource location when launching directly. Let's try
                // that as a backup.
                in = clazz.getResourceAsStream(fullPath.substring(1));
            }
            if (in == null) {
                // Another mangle.
                in = clazz.getResourceAsStream("/resources" + fullPath);
            }

            if (in == null) {
                return new SaucerSchemeResponse(SaucerRequestError.SAUCER_REQUEST_ERROR_NOT_FOUND);
            }

            byte[] data = StreamUtil.toBytes(in);
            String mime = MimeTypes.getMimeForFile(fullPath);
            return new SaucerSchemeResponse(new SaucerStash(data), mime);
        };
    }

}