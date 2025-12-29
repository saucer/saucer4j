package com.example.saucer4j;

import java.io.IOException;

import app.saucer.SaucerApp;
import app.saucer.util.SaucerUrl;
import app.saucer.webview.SaucerWebview;
import app.saucer.webview.scheme.SaucerSchemeHandler;
import app.saucer.webview.window.SaucerWindow;
import app.saucer.webview.window.SaucerWindowDecoration;

public class UndecoratedExample {

    public static void main(String[] args) throws IOException {
        SaucerApp.initialize("com.example.saucer4j", true);

        SaucerWebview.registerCustomScheme("app");

        SaucerWindow window = SaucerWindow.create();
        SaucerWebview webview = window.createWebview(
            (opts) -> opts.hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                                      // need this feature and if your environments support it.
        );

        webview
            .addSchemeHandler("app", SaucerSchemeHandler.fromResources(UndecoratedExample.class)) // Read the contents from our resources.
            .contextMenuAllowed(true) // Allow the right-click menu.
            .url(SaucerUrl.parse("app://authority/UndecoratedExample.html"));

        window
            .decorations(SaucerWindowDecoration.PARTIAL)
            .show()
            .focus();

        SaucerApp.run(); // This blocks until the last window is closed.
    }

}
