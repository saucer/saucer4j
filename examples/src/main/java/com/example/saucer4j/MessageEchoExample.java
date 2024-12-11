package com.example.saucer4j;

import java.io.IOException;

import app.saucer.Saucer;
import app.saucer.SaucerWindow.SaucerWindowListener;
import app.saucer.scheme.SaucerSchemeHandler;
import app.saucer.utils.SaucerApp;
import app.saucer.utils.SaucerPreferences;

public class MessageEchoExample {

    public static void main(String[] args) throws IOException {
        SaucerApp.initialize("com.example.saucer4j");

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

        saucer.window().setListener(new SaucerWindowListener() {
            @Override
            public void onClosed() {
                SaucerApp.quit(); // Causes run() to exit, and the JVM will follow suit.
            }
        });

        SaucerApp.run();
    }

}
