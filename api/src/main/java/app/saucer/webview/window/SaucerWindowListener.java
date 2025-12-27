package app.saucer.webview.window;

public interface SaucerWindowListener {

    default void onDecorated(SaucerWindowDecoration decoration) {}

    default void onResize(int width, int height) {}

    default void onMaximize(boolean isMaximized) {}

    default void onMinimize(boolean isMinimized) {}

    default void onFocus(boolean hasFocus) {}

    /**
     * @return true, if you want to prevent the webview from closing.
     */
    default boolean shouldAvoidClosing() {
        return false;
    }

    default void onClosed() {}

}
