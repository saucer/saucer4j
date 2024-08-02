package com.example.saucer_java;

import java.io.IOException;

import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.SaucerWebview.SaucerWebviewListener;
import co.casterlabs.saucer.utils.SaucerIcon;
import co.casterlabs.saucer.utils.SaucerOptions;

public class IconAndTitleExample {

    public static void main(String[] args) throws IOException {
        Saucer saucer = Saucer.create(
            new SaucerOptions()
                .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                            // need this feature and if your environments support it.
        );

        saucer.webview().setContextMenuAllowed(true); // Allow the right-click menu.

        saucer.webview().setListener(new SaucerWebviewListener() {
            @Override
            public void onTitleChanged(String newTitle) {
                saucer.window().setTitle(newTitle);
            }

            @Override
            public void onIconChanged(SaucerIcon newIcon) {
                System.out.println(newIcon.isEmpty());
                System.out.println(newIcon);
                saucer.window().setIcon(newIcon);
            }
        });

        saucer.webview().setUrl("https://google.com");

        saucer.run(); // This blocks until the user closes the window.
    }

}
