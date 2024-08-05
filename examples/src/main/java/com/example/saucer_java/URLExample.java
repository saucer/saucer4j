package com.example.saucer_java;

import java.io.IOException;

import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.Saucer.SaucerRunStrategy;
import co.casterlabs.saucer.utils.SaucerOptions;

public class URLExample {

    public static void main(String[] args) throws IOException {
        // Queue the window for creation. You must call run() before a window can be
        // created. dispatch() allows you to asynchronously wait for run() to be called.
        Saucer.dispatch(() -> {
            Saucer saucer = Saucer.create(
                new SaucerOptions()
                    .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                                // need this feature and if your environments support it.
            );

            saucer.webview().setContextMenuAllowed(true); // Allow the right-click menu.

            saucer.webview().setUrl("https://duckduckgo.com");

            saucer.window().show();
        });

        // This blocks until every window is closed.
        Saucer.run(SaucerRunStrategy.RUN_UNTIL_ALL_CLOSED);
    }

}
