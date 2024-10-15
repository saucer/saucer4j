package com.example.saucer_java;

import java.io.IOException;

import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.scheme.SaucerSchemeHandler;
import co.casterlabs.saucer.utils.SaucerApp;
import co.casterlabs.saucer.utils.SaucerPreferences;

public class MessageEchoExample {

    public static void main(String[] args) throws IOException {
        SaucerApp.initialize("com.example.saucer4j", () -> {
            Saucer.registerCustomScheme("app");

            Saucer saucer = Saucer.create(
                SaucerPreferences.create()
                    .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                                // need this feature and if your environments support it.
            );

            saucer.messages().onMessage((data) -> {
                saucer.messages().emit(data); // Echo it back.
            });

            saucer.webview().setContextMenuAllowed(true); // Allow the right-click menu.

            saucer.webview().setSchemeHandler(SaucerSchemeHandler.fromResources(MessageEchoExample.class)); // Read the contents from our resources.
            saucer.webview().setUrl("app://authority/MessageEchoExample.html");

            saucer.window().show();
        });
    }

}
