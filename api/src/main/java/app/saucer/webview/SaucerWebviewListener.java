package app.saucer.webview;

import app.saucer.webview.window.SaucerIcon;

public interface SaucerWebviewListener {

    default void onDomReady() {}

    default void onNavigated(String newUrl) {}

    /**
     * @return true, if the navigation should be completed normally.
     */
    default boolean onNavigate(SaucerNavigation navigation) {
        return true;
    }

    default void onFavicon(SaucerIcon newIcon) {}

    default void onTitle(String newTitle) {}

    default void onLoad(SaucerWebviewLoadState state) {}

    public static enum SaucerWebviewLoadState {
        STARTED,
        FINISHED
    }

}
