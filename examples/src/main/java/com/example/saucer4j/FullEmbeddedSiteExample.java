package com.example.saucer4j;

import java.io.IOException;

import app.saucer.Saucer;
import app.saucer.scheme.SaucerSchemeHandler;
import app.saucer.utils.SaucerApp;
import app.saucer.utils.SaucerPreferences;

public class FullEmbeddedSiteExample {

    public static void main(String[] args) throws IOException {
        SaucerApp.initialize("com.example.saucer4j", () -> {
            Saucer.registerCustomScheme("app");

            Saucer saucer = Saucer.create(
                SaucerPreferences.create()
                    .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                                // need this feature and if your environments support it.
            );

            saucer.webview().setDevtoolsVisible(true);
            saucer.webview().setContextMenuAllowed(true); // Allow the right-click menu.

            saucer.webview().setSchemeHandler(SaucerSchemeHandler.fromResources(FullEmbeddedSiteExample.class, "/full"));  // Scan for files under the name `/full` in the current jar.
            saucer.webview().setUrl("app://authority/index.html"); // Tell Saucer to serve the index file.

            saucer.window().show();
        });
    }

}
