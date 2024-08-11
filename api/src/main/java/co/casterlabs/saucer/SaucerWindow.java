package co.casterlabs.saucer;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.saucer.documentation.AvailableFromJS;
import co.casterlabs.saucer.utils.SaucerIcon;
import co.casterlabs.saucer.utils.SaucerSize;
import lombok.NonNull;
import lombok.SneakyThrows;

public interface SaucerWindow {

    /**
     * Synchronously dispatches the provided task on the main run thread.
     * 
     * @see #run()
     */
    @SneakyThrows
    default void dispatch(@NonNull Runnable task) {
        dispatch(() -> {
            task.run();
            return null;
        });
    }

    /**
     * Synchronously dispatches the provided task on the main run thread, returning
     * the result.
     * 
     * @see #run()
     */
    public <T> T dispatch(@NonNull Supplier<T> task);

    /**
     * @return whether or not Saucer is in the foreground/focused.
     */
    @AvailableFromJS
    public boolean isFocused();

    /**
     * Focuses Saucer, bringing it into the foreground.
     */
    @AvailableFromJS
    public void focus();

    /**
     * @return whether or not Saucer is minimized.
     */
    @AvailableFromJS
    public boolean isMinimized();

    /**
     * Whether or not to minimize (true) or restore (false)
     */
    @AvailableFromJS
    public void setMinimized(boolean b);

    /**
     * @return whether or not Saucer is maximized.
     */
    @AvailableFromJS
    public boolean isMaximized();

    /**
     * Whether or not to maximize (true) or restore (false)
     */
    @AvailableFromJS
    public void setMaximized(boolean b);

    /**
     * @return whether or not Saucer is resizable by the user.
     */
    @AvailableFromJS
    public boolean isResizable();

    /**
     * Enables (true) or disables (false) the resizing of Saucer.
     */
    @AvailableFromJS
    public void setResizable(boolean b);

    /**
     * @return whether or not Saucer has decorations (i.e the title bar).
     */
    @AvailableFromJS
    public boolean hasDecorations();

    /**
     * Enables (true) or disables (false) Saucer's window decorations (i.e the title
     * bar).
     */
    @AvailableFromJS
    public void showDecorations(boolean b);

    /**
     * @return whether or not Saucer is always on top of every other window.
     */
    @AvailableFromJS
    public boolean isAlwaysOnTop();

    /**
     * Sets whether or not Saucer is always on top of every other window.
     */
    @AvailableFromJS
    public void setAlwaysOnTop(boolean b);

    /**
     * @return the title of the Saucer window.
     */
    @AvailableFromJS
    public String getTitle();

    /**
     * Sets the title of the Saucer window.
     */
    @AvailableFromJS
    public void setTitle(@NonNull String title);

    /**
     * @return the size of the Saucer window.
     */
    @AvailableFromJS
    public SaucerSize getSize();

    /**
     * Sets the size of the Saucer window.
     */
    @AvailableFromJS
    public void setSize(@NonNull SaucerSize size);

    /**
     * @return the minimum allowed size of the Saucer window.
     */
    @AvailableFromJS
    public SaucerSize getMinSize();

    /**
     * Sets the minimum allowed size of the Saucer window.
     */
    @AvailableFromJS
    public void setMinSize(@NonNull SaucerSize size);

    /**
     * @return the maximum allowed size of the Saucer window.
     */
    @AvailableFromJS
    public SaucerSize getMaxSize();

    /**
     * Sets the maximum allowed size of the Saucer window.
     */
    @AvailableFromJS
    public void setMaxSize(@NonNull SaucerSize size);

    /**
     * Hides Saucer, this causes the window to disappear from the taskbar and the
     * user will no longer be able to view the app no matter what they do.
     * 
     * @see #show()
     */
    @AvailableFromJS
    public void hide();

    /**
     * Unhides Saucer.
     * 
     * @see #hide()
     */
    @AvailableFromJS
    public void show();

    /**
     * Changes the application's icon (e.g what you see in the dock/taskbar).
     * 
     * @see {@link SaucerWebview#getFavicon()}
     */
    public void setIcon(@NonNull SaucerIcon icon);

    /**
     * Allows you to receive events from the Window, focus or minimization.
     */
    public void setListener(@Nullable SaucerWindowListener listener);

    public static interface SaucerWindowListener {

        default void onResize(int width, int height) {}

        default void onMaximize() {}

        default void onMinimize() {}

        /**
         * Called when a minimized window is made visible again.
         */
        default void onRestore() {}

        default void onFocused() {}

        default void onBlur() {}

        /**
         * @return true, if you want to prevent the webview from closing.
         */
        default boolean shouldAvoidClosing() {
            return false;
        }

        default void onClosed() {}

    }

}
