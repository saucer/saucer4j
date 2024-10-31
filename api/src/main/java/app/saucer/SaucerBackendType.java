package app.saucer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SaucerBackendType {
    WEBKITGTK("WebKitGtk"),
    QT5("Qt5"),
    QT6("Qt6"),

    WEBKIT("WebKit"),

    WEBVIEW2("WebView2"),
    ;

    private final String pretty;

    @Override
    public String toString() {
        return this.pretty;
    }

}
