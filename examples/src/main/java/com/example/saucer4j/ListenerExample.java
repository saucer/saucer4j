package com.example.saucer4j;

import java.io.IOException;

import app.saucer.SaucerApp;
import app.saucer.util.SaucerUrl;
import app.saucer.webview.SaucerNavigation;
import app.saucer.webview.SaucerWebview;
import app.saucer.webview.SaucerWebviewListener;
import app.saucer.webview.SaucerWebviewLoadState;
import app.saucer.webview.window.SaucerIcon;
import app.saucer.webview.window.SaucerWindow;
import app.saucer.webview.window.SaucerWindowDecoration;
import app.saucer.webview.window.SaucerWindowListener;

public class ListenerExample {

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
                public void onDomReady() {
                    System.out.println("[Webview] Dom ready!");
                }

                @Override
                public void onNavigated(SaucerUrl newUrl) {
                    System.out.println("[Webview] Navigated: " + newUrl);
                }

                /**
                 * @return true, if the navigation should be completed normally.
                 */
                @Override
                public boolean onNavigate(SaucerNavigation navigation) {
                    System.out.println("[Webview] Navigation requested: " + navigation);
                    return true;
                }

                @Override
                public void onFavicon(SaucerIcon newIcon) {
                    System.out.println("[Webview] Favicon changed: " + newIcon);
                }

                @Override
                public void onTitle(String newTitle) {
                    System.out.println("[Webview] Title: " + newTitle);
                }

                @Override
                public void onLoad(SaucerWebviewLoadState state) {
                    System.out.println("[Webview] Load state: " + state);
                }
            })
            .contextMenuAllowed(true) // Allow the right-click menu.
            .url(SaucerUrl.parse("https://duckduckgo.com"));

        window
            .listener(new SaucerWindowListener() {
                @Override
                public void onDecorated(SaucerWindowDecoration decoration) {
                    System.out.println("[Window] Decoration: " + decoration);
                }

                @Override
                public void onResize(int width, int height) {
                    System.out.println("[Window] Resized: " + width + "x" + height);
                }

                @Override
                public void onMaximize(boolean isMaximized) {
                    System.out.println("[Window] Maximized: " + isMaximized);
                }

                @Override
                public void onMinimize(boolean isMinimized) {
                    System.out.println("[Window] Minimized: " + isMinimized);
                }

                @Override
                public void onFocus(boolean hasFocus) {
                    System.out.println("[Window] Focus: " + hasFocus);
                }

                /**
                 * @return true, if you want to prevent the webview from closing.
                 */
                @Override
                public boolean shouldAvoidClosing() {
                    System.out.println("[Window] Close requested...");
                    return false;
                }

                @Override
                public void onClosed() {
                    System.out.println("[Window] Closed!");
                }
            })
            .show()
            .focus();

        SaucerApp.run(); // This blocks until the last window is closed.
    }

}
