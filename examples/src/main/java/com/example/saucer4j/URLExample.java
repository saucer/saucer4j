package com.example.saucer4j;

import java.io.IOException;

import app.saucer.SaucerApp;
import app.saucer.util.SaucerUrl;
import app.saucer.webview.SaucerWebview;
import app.saucer.webview.window.SaucerWindow;

public class URLExample {

    public static void main(String[] args) throws IOException {
        SaucerApp.initialize("com.example.saucer4j", true);

        SaucerWindow window = SaucerWindow.create();

        SaucerWebview webview = window.createWebview(
            (opts) -> opts.hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                                      // need this feature and if your environments support it.
        );

        webview.setContextMenuAllowed(true); // Allow the right-click menu.
        webview.setUrl(SaucerUrl.parse("https://duckduckgo.com"));

        window.show();
        window.focus();

        SaucerApp.run(); // This blocks until the last window is closed.
    }

}
