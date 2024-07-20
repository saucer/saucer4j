package com.example.saucer_java;

import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.utils.SaucerEmbeddedFiles;
import co.casterlabs.saucer.utils.SaucerOptions;

public class EmbeddingExample {

    public static void main(String[] args) {
        Saucer saucer = Saucer.create(
            new SaucerOptions()
                .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                            // need this feature and if your environments support it.
        );

        saucer.webview().setContextMenuAllowed(true); // Allow the right-click menu.

        SaucerEmbeddedFiles files = new SaucerEmbeddedFiles()
            // Add a file named "index.html" with a simple html page.
            .add(
                "index.html",
                "text/html",
                "<!DOCTYPE html>"
                    + "<html>"
                    + "<h1>Hello world!</h1>"
                    + "<sub>With ❤️ from saucer_java</sub>"
                    + "</html>"
            );
        saucer.webview().serve(files, "index.html"); // Tell Saucer to serve that file we made above.

        saucer.run(); // This blocks until the user closes the window.
    }

}
