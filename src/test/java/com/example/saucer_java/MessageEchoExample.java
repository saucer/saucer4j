package com.example.saucer_java;

import java.io.IOException;

import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.scheme.SaucerSchemeHandler;
import co.casterlabs.saucer.utils.SaucerOptions;

public class MessageEchoExample {

    public static void main(String[] args) throws IOException {
        Saucer saucer = Saucer.create(
            new SaucerOptions()
                .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                            // need this feature and if your environments support it.
        );

        saucer.webview().setContextMenuAllowed(true); // Allow the right-click menu.

        saucer.webview().setSchemeHandler(SaucerSchemeHandler.fromResources(MessageEchoExample.class)); // Read the contents from our resources.
        saucer.webview().serveScheme("MessageEchoExample.html");

        saucer.messages().onMessage((data) -> {
            saucer.messages().emit(data); // Echo it back.
        });

        saucer.run(); // This blocks until the user closes the window.
    }

}
