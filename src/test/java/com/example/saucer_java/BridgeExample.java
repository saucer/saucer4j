package com.example.saucer_java;

import java.io.IOException;

import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.bridge.JavascriptGetter;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.utils.SaucerEmbeddedFiles;
import co.casterlabs.saucer.utils.SaucerOptions;

public class BridgeExample {

    public static void main(String[] args) throws IOException {
        Saucer saucer = Saucer.create(
            new SaucerOptions()
                .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                            // need this feature and if your environments support it.
        );

        saucer.webview().setContextMenuAllowed(true); // Allow the right-click menu.

        SaucerEmbeddedFiles files = new SaucerEmbeddedFiles()
            // Add a file named "index.html" with a simple html page.
            .add(
                "BridgeExample.html",
                "text/html",
                BridgeExample.class.getResourceAsStream("/BridgeExample.html")
            );
        saucer.webview().serve(files, "BridgeExample.html"); // Tell Saucer to serve that file we made above.

        saucer.bridge().defineConstant("thisIsAConstant", JsonObject.EMPTY_OBJECT);
        saucer.bridge().defineObject("Example", new BridgeObjectExample(saucer));
        saucer.bridge().apply();

        saucer.run(); // This blocks until the user closes the window.
    }

    @JavascriptObject
    public static class BridgeObjectExample {
        private Saucer saucer;

        // Grab the saucer instance so we can open the devTools below.
        public BridgeObjectExample(Saucer saucer) {
            this.saucer = saucer;
        }

        @JavascriptGetter
        public long nanoTime() {
            return System.nanoTime();
        }

        @JavascriptFunction
        public void openDevTools() {
            this.saucer.webview().setDevtoolsVisible(true);
        }

    }

}
