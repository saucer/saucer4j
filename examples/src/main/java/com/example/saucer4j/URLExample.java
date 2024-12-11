package com.example.saucer4j;

import java.io.IOException;

import app.saucer.Saucer;
import app.saucer.utils.SaucerApp;
import app.saucer.utils.SaucerPreferences;

public class URLExample {

    public static void main(String[] args) throws IOException {
        SaucerApp.initialize("com.example.saucer4j");

        Saucer saucer = Saucer.create(
            SaucerPreferences.create()
                .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                            // need this feature and if your environments support it.
        );

        saucer.webview().setContextMenuAllowed(true); // Allow the right-click menu.

        saucer.webview().setUrl("https://duckduckgo.com");

        saucer.window().show();

        SaucerApp.run(); // This blocks until the last window is closed.
    }

}
