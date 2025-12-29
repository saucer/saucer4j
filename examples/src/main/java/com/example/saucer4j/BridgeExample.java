package com.example.saucer4j;

import java.io.IOException;

import app.saucer.SaucerApp;
import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptGetter;
import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptValue;
import app.saucer.util.SaucerUrl;
import app.saucer.webview.SaucerWebview;
import app.saucer.webview.scheme.SaucerSchemeHandler;
import app.saucer.webview.window.SaucerWindow;

public class BridgeExample {

    public static void main(String[] args) throws IOException {
        SaucerApp.initialize("com.example.saucer4j", true);
        SaucerWebview.registerCustomScheme("app");

        SaucerWindow window = SaucerWindow.create();
        SaucerWebview webview = window.createWebview(
            (opts) -> opts.hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                                      // need this feature and if your environments support it.
        );

        webview.bridge.defineObject("Example", new BridgeObjectExample(webview));

        webview
            .addSchemeHandler("app", SaucerSchemeHandler.fromResources(BridgeExample.class)) // Read the contents from our resources.
            .contextMenuAllowed(true) // Allow the right-click menu.
            .url(SaucerUrl.parse("app://authority/BridgeExample.html"));

        window
            .show()
            .focus();

        SaucerApp.run(); // This blocks until the last window is closed.
    }

    @JavascriptObject
    public static class BridgeObjectExample {
        private SaucerWebview webview;

        @SuppressWarnings("unused")
        private final NestedBridgeObjectExample nested = new NestedBridgeObjectExample();

        /**
         * Saucer will automatically read the field and send updates to the webview to
         * reduce IPC calls (speed up).
         */
        @JavascriptValue(watchForMutate = true)
        public long nanoTime;

        // We grab the saucer instance so we can open the devTools below.
        public BridgeObjectExample(SaucerWebview webview) {
            this.webview = webview;

            Thread t = new Thread(() -> {
                while (true) {
                    this.nanoTime = System.nanoTime();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {}
                }
            });
            t.setDaemon(true);
            t.start();
        }

        @JavascriptGetter
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }

        @JavascriptFunction
        public void openDevTools() {
            this.webview.devToolsVisible(true);
        }

    }

    @JavascriptObject
    public static class NestedBridgeObjectExample {

        @JavascriptGetter
        public String hello() {
            return "Hello world!";
        }

    }

}
