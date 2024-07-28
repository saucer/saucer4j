package com.example.saucer_java;

import java.io.IOException;

import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.utils.SaucerEmbeddedFiles;
import co.casterlabs.saucer.utils.SaucerOptions;

public class FullEmbeddedSiteExample {

    public static void main(String[] args) throws IOException {
        Saucer saucer = Saucer.create(
            new SaucerOptions()
                .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                            // need this feature and if your environments support it.
        );

        saucer.webview().setContextMenuAllowed(true); // Allow the right-click menu.

        SaucerEmbeddedFiles files = new SaucerEmbeddedFiles()
            .scanForResources("/full"); // Scan for files under the name `/full` in the current jar.
        saucer.webview().serve(files, "index.html"); // Tell Saucer to serve the index file.

        saucer.run(); // This blocks until the user closes the window.
    }

}