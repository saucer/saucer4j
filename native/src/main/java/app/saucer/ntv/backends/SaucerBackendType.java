package app.saucer.ntv.backends;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SaucerBackendType {
    WEBKITGTK("WebKitGtk"),
    QT6("Qt6"),

    WEBKIT("WebKit"),

    WEBVIEW2("WebView2"),

    /**
     * If you hack in your own backend, use this as the type :^)
     */
    CUSTOM("Custom"),
    ;

    private final String pretty;

    @Override
    public String toString() {
        return this.pretty;
    }

}
