package com.example.saucer4j;

import java.io.IOException;

import app.saucer.SaucerApp;
import app.saucer.util.SaucerUrl;
import app.saucer.webview.SaucerWebview;
import app.saucer.webview.SaucerWebviewListener;
import app.saucer.webview.window.SaucerIcon;
import app.saucer.webview.window.SaucerWindow;

public class IconAndTitleExample {

    public static void main(String[] args) throws IOException {
        SaucerApp.initialize("com.example.saucer4j", true);

        SaucerWindow window = SaucerWindow.create();
        SaucerWebview webview = window.createWebview(
            (opts) -> opts.hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                                      // need this feature and if your environments support it.
        );

        webview
            .listener(new SaucerWebviewListener() {
                @Override
                public void onTitle(String newTitle) {
                    window.title(newTitle);
                }

                @Override
                public void onFavicon(SaucerIcon newIcon) {
                    if (!newIcon.isEmpty()) {
                        window.icon(newIcon);
                    }
                }
            })
            .contextMenuAllowed(true) // Allow the right-click menu.
            .url(SaucerUrl.parse("https://google.com"));

        window
            .show()
            .focus();

        SaucerApp.run(); // This blocks until the last window is closed.
    }

}
