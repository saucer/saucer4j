package co.casterlabs.saucer;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.documentation.AvailableFromJS;
import co.casterlabs.saucer.documentation.ThreadSafe;
import co.casterlabs.saucer.scheme.SaucerSchemeHandler;
import co.casterlabs.saucer.utils.SaucerColor;
import lombok.NonNull;

public interface SaucerWebview {

    /**
     * @return whether or not the devtools window is open.
     */
    @ThreadSafe
    @AvailableFromJS
    public boolean isDevtoolsVisible();

    /**
     * Sets whether or not the devtools window should be shown.
     */
    @ThreadSafe
    @AvailableFromJS
    public void setDevtoolsVisible(boolean show);

    /**
     * @return the current URL the webview is navigated to.
     */
    @ThreadSafe
    @AvailableFromJS
    public String currentUrl();

    /**
     * Navigates the webview to the given URL.
     */
    @ThreadSafe
    @AvailableFromJS
    public void setUrl(@NonNull String url);

    @ThreadSafe
    @AvailableFromJS
    public void serveScheme(@NonNull String path);

    /**
     * @return whether or not the context menu is enabled.
     */
    @ThreadSafe
    @AvailableFromJS
    public boolean isContextMenuAllowed();

    /**
     * Enables the default context menu (i.e right-click menu).
     */
    @ThreadSafe
    @AvailableFromJS
    public void setContextMenuAllowed(boolean allowed);

    /**
     * Executes the given JavaScript code in the webview.
     */
    @ThreadSafe
    @AvailableFromJS
    public void executeJavaScript(@NonNull String scriptToExecute);

    /**
     * Reloads the current URL.
     */
    @ThreadSafe
    @JavascriptFunction
    @AvailableFromJS
    default void reload() {
        this.executeJavaScript("location.reload();");
    }

    /**
     * @return the background color of the Saucer window. Note that this is
     *         different from the HTML background.
     */
    @ThreadSafe
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
    @ThreadSafe
    @AvailableFromJS
    public void setBackground(@NonNull SaucerColor color);

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

        default void onLoadFinished() {}

        default void onLoadStarted() {}

        default void onUrlChanged(String newUrl) {}

        default void onDomReady() {}

    }

}
