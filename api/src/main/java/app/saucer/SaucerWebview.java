package app.saucer;

import org.jetbrains.annotations.Nullable;

import app.saucer.bridge.JavascriptFunction;
import app.saucer.documentation.AvailableFromJS;
import app.saucer.scheme.SaucerSchemeHandler;
import app.saucer.utils.SaucerColor;
import app.saucer.utils.SaucerIcon;
import app.saucer.utils.SaucerNavigation;
import lombok.NonNull;

public interface SaucerWebview {

    /**
     * @return whether or not the devtools window is open.
     */
    @AvailableFromJS
    public boolean isDevtoolsVisible();

    /**
     * Sets whether or not the devtools window should be shown.
     */
    @AvailableFromJS
    public void setDevtoolsVisible(boolean show);

    /**
     * @return the current URL the webview is navigated to.
     */
    @AvailableFromJS
    public String currentUrl();

    /**
     * Navigates the webview to the given URL.
     */
    @AvailableFromJS
    public void setUrl(@NonNull String url);

    /**
     * @return whether or not the context menu is enabled.
     */
    @AvailableFromJS
    public boolean isContextMenuAllowed();

    /**
     * Enables the default context menu (i.e right-click menu).
     */
    @AvailableFromJS
    public void setContextMenuAllowed(boolean allowed);

    /**
     * Navigates backward.
     */
    @JavascriptFunction
    @AvailableFromJS
    public void back();

    /**
     * Navigates forward.
     */
    @JavascriptFunction
    @AvailableFromJS
    public void forward();

    /**
     * Reloads the current page.
     */
    @JavascriptFunction
    @AvailableFromJS
    public void reload();

    /**
     * @return the background color of the Saucer window. Note that this is
     *         different from the HTML background.
     */
    @AvailableFromJS
    public SaucerColor getBackground();

    /**
     * Allows you to set the background color of the Saucer window. Note that this
     * is different from the HTML background.
     * 
     * This is useful if you want a transparent or translucent application.
     * 
     * @implNote Applying blur effects in HTML will not cause the apps behind the
     *           window to appear blurred.
     */
    @AvailableFromJS
    public void setBackground(@NonNull SaucerColor color);

    /**
     * Gets the current favicon of the loaded page. Useful with
     * {@link SaucerWindow#setIcon(SaucerIcon)}.
     */
    public SaucerIcon getFavicon();

    /**
     * @return true if the `prefers-color-scheme` media query is forcibly set to
     *         `dark`.
     */
    @AvailableFromJS
    public boolean isForceDarkAppearance();

    /**
     * Forces the `prefers-color-scheme` media query to `dark` if true.
     * 
     * @implNote The Qt5 backend does not support this, so the call will do nothing
     *           on the Qt5 backend.
     * @implNote For Qt6, a version of 6.7 or greater is required for this to work.
     */
    @AvailableFromJS
    public void setForceDarkAppearance(boolean shouldAppearDark);

    /**
     * Allows you to receive events from the Webview, such as page load or
     * navigation events.
     */
    public void setListener(@Nullable SaucerWebviewListener listener);

    /**
     * Allows you to serve custom webpages from your own resources.
     */
    public void setSchemeHandler(@Nullable SaucerSchemeHandler handler);

    public static interface SaucerWebviewListener {

        default void onDomReady() {}

        default void onNavigated(String newUrl) {}

        /**
         * @return true, if the navigation should be handled normally.
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

}
