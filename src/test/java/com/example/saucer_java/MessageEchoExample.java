package com.example.saucer_java;

import java.io.IOException;

import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.utils.SaucerEmbeddedFiles;
import co.casterlabs.saucer.utils.SaucerOptions;

public class MessageEchoExample {

    public static void main(String[] args) throws IOException {
        Saucer saucer = Saucer.create(
            new SaucerOptions()
                .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                            // need this feature and if your environments support it.
        );

        saucer.webview().setContextMenuAllowed(true); // Allow the right-click menu.

        SaucerEmbeddedFiles files = new SaucerEmbeddedFiles()
            // Add a file named "index.html" with a simple html page.
            .addResource(
                "MessageEchoExample.html",
                "text/html"
            );
        saucer.webview().serve(files, "MessageEchoExample.html"); // Tell Saucer to serve that file we made above.

        saucer.messages().onMessage((data) -> {
            saucer.messages().emit(data); // Echo it back.
        });

        saucer.run(); // This blocks until the user closes the window.
    }

}
