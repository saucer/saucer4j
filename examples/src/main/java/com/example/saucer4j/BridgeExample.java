package com.example.saucer4j;

import java.io.IOException;

import app.saucer.Saucer;
import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptGetter;
import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptValue;
import app.saucer.scheme.SaucerSchemeHandler;
import app.saucer.utils.SaucerApp;
import app.saucer.utils.SaucerPreferences;

public class BridgeExample {

    public static void main(String[] args) throws IOException {
        SaucerApp.initialize("com.example.saucer4j", () -> {
            Saucer.registerCustomScheme("app");

            Saucer saucer = Saucer.create(
                SaucerPreferences.create()
                    .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                                // need this feature and if your environments support it.
            );

            saucer.bridge().defineObject("Example", new BridgeObjectExample(saucer));

            saucer.webview().setContextMenuAllowed(true); // Allow the right-click menu.

            saucer.webview().setSchemeHandler(SaucerSchemeHandler.fromResources(BridgeExample.class)); // Read the contents from our resources.
            saucer.webview().setUrl("app://authority/BridgeExample.html");

            saucer.window().show();
        });
    }

    @JavascriptObject
    public static class BridgeObjectExample {
        private Saucer saucer;

        private final NestedBridgeObjectExample nested = new NestedBridgeObjectExample();

        /**
         * Saucer will automatically read the field and send updates to the webview to
         * reduce IPC calls (speed up).
         */
        @JavascriptValue(watchForMutate = true)
        public long nanoTime;

        // We grab the saucer instance so we can open the devTools below.
        public BridgeObjectExample(Saucer saucer) {
            this.saucer = saucer;

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
            this.saucer.webview().setDevtoolsVisible(true);
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
