package com.example.saucer_java;

import java.io.IOException;

import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.SaucerWebview.SaucerWebviewListener;
import co.casterlabs.saucer.utils.SaucerApp;
import co.casterlabs.saucer.utils.SaucerIcon;
import co.casterlabs.saucer.utils.SaucerPreferences;

public class IconAndTitleExample {

    public static void main(String[] args) throws IOException {
        SaucerApp.initialize("com.example.saucer4j", () -> {
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
        });
    }

}
