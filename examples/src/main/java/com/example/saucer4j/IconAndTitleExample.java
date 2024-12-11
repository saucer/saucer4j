package com.example.saucer4j;

import java.io.IOException;

import app.saucer.Saucer;
import app.saucer.SaucerWebview.SaucerWebviewListener;
import app.saucer.SaucerWindow.SaucerWindowListener;
import app.saucer.utils.SaucerApp;
import app.saucer.utils.SaucerIcon;
import app.saucer.utils.SaucerPreferences;

public class IconAndTitleExample {

    public static void main(String[] args) throws IOException {
        SaucerApp.initialize("com.example.saucer4j");

        Saucer saucer = Saucer.create(
            SaucerPreferences.create()
                .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                            // need this feature and if your environments support it.
        );

        saucer.webview().setListener(new SaucerWebviewListener() {
            @Override
            public void onTitle(String newTitle) {
                saucer.window().setTitle(newTitle);
            }

            @Override
            public void onFavicon(SaucerIcon newIcon) {
                if (!newIcon.isEmpty()) {
                    saucer.window().setIcon(newIcon);
                }
            }
        });

        saucer.webview().setContextMenuAllowed(true); // Allow the right-click menu.

        saucer.webview().setUrl("https://google.com");

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
