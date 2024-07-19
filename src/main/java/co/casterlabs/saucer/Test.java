package co.casterlabs.saucer;

import co.casterlabs.saucer.utils.SaucerEmbeddedFiles;
import co.casterlabs.saucer.utils.SaucerOptions;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        SaucerEmbeddedFiles files = new SaucerEmbeddedFiles()
            .add(
                "index.html", "text/html",
                "<!DOCTYPE html>"
                    + "<html>"
                    + "<h1>Hello world!</h1>"
                    + "<h2>With ❤️ from saucer_java</h2>"
                    + "</html>"
            );

        Saucer view = Saucer.create(
            new SaucerOptions()
                .hardwareAcceleration(true)
        );

        view.webview().setDevtoolsVisible(true);
        view.webview().setContextMenuAllowed(true);
        view.webview().serve(files, "index.html");

        view.run();
    }

}
