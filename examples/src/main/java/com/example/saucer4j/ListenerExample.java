package com.example.saucer4j;

import java.io.IOException;

import app.saucer.Saucer;
import app.saucer.SaucerApp;
import app.saucer.SaucerPreferences;
import app.saucer.webview.SaucerNavigation;
import app.saucer.webview.SaucerWebviewListener;
import app.saucer.webview.window.SaucerIcon;
import app.saucer.webview.window.SaucerWindowListener;

public class ListenerExample {

    public static void main(String[] args) throws IOException {
        SaucerApp.initialize("com.example.saucer4j");

        Saucer saucer = Saucer.create(
            SaucerPreferences.create()
                .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                            // need this feature and if your environments support it.
        );

        saucer.webview().setListener(new SaucerWebviewListener() {
            @Override
            public void onDomReady() {
                System.out.println("[Webview] Dom ready!");
            }

            @Override
            public void onNavigated(String newUrl) {
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
        });

        saucer.window().setListener(new SaucerWindowListener() {
            @Override
            public void onDecorated(boolean isDecorated) {
                System.out.println("[Window] Decorated: " + isDecorated);
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
        });

        saucer.webview().setContextMenuAllowed(true); // Allow the right-click menu.

        saucer.webview().setUrl("https://duckduckgo.com");

        saucer.window().show();

        SaucerApp.run(); // This blocks until the last window is closed.
    }

}
