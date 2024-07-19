package co.casterlabs.saucer;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.documentation.AvailableFromJS;
import co.casterlabs.saucer.documentation.ThreadSafe;
import co.casterlabs.saucer.utils.SaucerEmbeddedFiles;
import lombok.NonNull;

public interface SaucerWebview {

    /**
     * @return whether or not the devtools window is open.
     */
    @ThreadSafe
    @AvailableFromJS
    public boolean isDevtoolsVisible();

    /**
     * @param enabled if true, the devtools window is opened. if false, it is
     *                closed.
     */
    @ThreadSafe
    @AvailableFromJS
    public void setDevtoolsVisible(boolean enabled);

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

    /**
     * Serves an embedded file.
     */
    @ThreadSafe
    public void serve(@NonNull SaucerEmbeddedFiles files, @NonNull String path);

    /**
     * @return whether or not the context menu is enabled.
     */
    @ThreadSafe
    @AvailableFromJS
    public boolean isContextMenuAllowed();

    /**
     * @param allowed if true.
     */
    @ThreadSafe
    @AvailableFromJS
    public void setContextMenuAllowed(boolean enabled);

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
     * Allows you to receive events from the Webview, such as page load or
     * navigation events.
     */
    public void setListener(@Nullable SaucerWebviewListener listener);

    public static interface SaucerWebviewListener {

        default void onLoadFinished() {}

        default void onLoadStarted() {}

        default void onUrlChanged(String newUrl) {}

        default void onDomReady() {}

    }

}
