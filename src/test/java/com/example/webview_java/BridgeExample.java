package com.example.webview_java;

import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.bridge.JavascriptGetter;
import co.casterlabs.saucer.bridge.JavascriptObject;
import co.casterlabs.saucer.utils.SaucerEmbeddedFiles;
import co.casterlabs.saucer.utils.SaucerOptions;

public class BridgeExample {

    public static void main(String[] args) {
        Saucer saucer = Saucer.create(
            new SaucerOptions()
                .hardwareAcceleration(true) // May not work on all computers. You should do some testing to discover if you
                                            // need this feature and if your environments support it.
        );

        saucer.webview().setContextMenuAllowed(true); // Allow the right-click menu.

        SaucerEmbeddedFiles files = new SaucerEmbeddedFiles()
            // Add a file named "index.html" with a simple html page.
            .add(
                "index.html",
                "text/html",
                "<!DOCTYPE html>"
                    + "<html>"
                    + "<h1>Hello world!</h1>"
                    + "<button onclick='Example.openDevTools()'>Open Developer Tools</button><br />"
                    + "<sub>With ❤️ from saucer_java</sub>"
                    + "</html>"
            );
        saucer.webview().serve(files, "index.html"); // Tell Saucer to serve that file we made above.

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
