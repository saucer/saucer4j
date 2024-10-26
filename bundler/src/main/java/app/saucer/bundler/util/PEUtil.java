package app.saucer.bundler.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import app.saucer.bundler.Bundler;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class PEUtil {
    private static final String RCEDIT_DOWNLOAD = "https://github.com/electron/rcedit/releases/download/v2.0.0/rcedit-x64.exe";
    private static final FastLogger LOGGER = new FastLogger("PEUtil");

    private static final HttpClient httpClient = HttpClient
        .newBuilder()
        .followRedirects(Redirect.ALWAYS)
        .build();

    public static void setExeIcon(File executableFile, File icon) throws IOException, InterruptedException {
        if (Platform.osDistribution != OSDistribution.WINDOWS_NT) {
            // Check for wine.
            int exitCode = Runtime.getRuntime().exec("wine").waitFor();
            if (exitCode != 0) {
                LOGGER.warn(
                    "You cannot modify the icon of an .exe file on non-Windows systems without wine installed. Ignoring.",
                    executableFile.getAbsolutePath()
                );
                return;
            }
        }

        File rcedit = new File(Bundler.DOWNLOAD_CACHE_FOLDER, "rcedit.exe");
        if (rcedit.exists()) {
            LOGGER.info("Using cached rcedit.exe");
        } else {
            LOGGER.info("Downloading rcedit.exe...");
            httpClient.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(RCEDIT_DOWNLOAD))
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofFile(rcedit.toPath())
            );
            LOGGER.info("Finished downloading rcedit.exe");
        }

        LOGGER.info("Modifying icon of %s...", executableFile.getAbsolutePath());

        if (Platform.osDistribution == OSDistribution.WINDOWS_NT) {
            Runtime.getRuntime().exec(new String[] {
                    rcedit.getAbsolutePath(),
                    "--set-icon",
                    icon.getAbsolutePath(),
                    executableFile.getAbsolutePath()
            }).waitFor();
        } else {
            Runtime.getRuntime().exec(new String[] {
                    "wine",
                    rcedit.getAbsolutePath(),
                    "--set-icon",
                    icon.getAbsolutePath(),
                    executableFile.getAbsolutePath()
            }).waitFor();
        }

        icon.delete(); // Cleanup.

        LOGGER.info("Finished modifying icon of %s", executableFile.getAbsolutePath());
    }

}
