package app.saucer.bundler.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import app.saucer.bundler.Bundler;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class Maven {
    private static final FastLogger LOGGER = new FastLogger("Maven");

    private static final HttpClient httpClient = HttpClient
        .newBuilder()
        .followRedirects(Redirect.ALWAYS)
        .build();

    public static synchronized File downloadDependency(String repo, String group, String artifact, String version, String suffix) throws InterruptedException, IOException {
        File dependency = new File(Bundler.DOWNLOAD_CACHE_FOLDER, String.format("maven/%s/%s/%s/%s-%s%s", group, artifact, version, artifact, version, suffix));
        String downloadUrl = String.format("%s/%s/%s/%s/%s-%s%s", repo, group.replace('.', '/'), artifact, version, artifact, version, suffix);

        LOGGER.debug("Url: %s, Path: %s", downloadUrl, dependency);

        if (dependency.exists()) {
            LOGGER.info("This JRE build is cached. Using that instead.");
        } else {
            LOGGER.info("Found a link. Downloading...");

            httpClient.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(downloadUrl))
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofFile(dependency.toPath())
            );
            LOGGER.info("Finished downloading...");
        }
        return dependency;
    }

}
