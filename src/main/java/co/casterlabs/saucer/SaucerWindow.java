package co.casterlabs.saucer;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.saucer.documentation.AvailableFromJS;
import co.casterlabs.saucer.documentation.ThreadSafe;
import co.casterlabs.saucer.utils.SaucerColor;
import co.casterlabs.saucer.utils.SaucerSize;
import lombok.NonNull;

public interface SaucerWindow {

    /**
     * @return whether or not Saucer is in the foreground/focused.
     */
    @ThreadSafe
    @AvailableFromJS
    public boolean isFocused();

    /**
     * Focuses Saucer, bringing it into the foreground.
     */
    @ThreadSafe
    @AvailableFromJS
    public void focus();

    /**
     * @return whether or not Saucer is minimized.
     */
    @ThreadSafe
    @AvailableFromJS
    public boolean isMinimized();

    /**
     * Whether or not to minimize (true) or restore (false)
     */
    @ThreadSafe
    @AvailableFromJS
    public void setMinimized(boolean b);

    /**
     * @return whether or not Saucer is maximized.
     */
    @ThreadSafe
    @AvailableFromJS
    public boolean isMaximized();

    /**
     * Whether or not to maximize (true) or restore (false)
     */
    @ThreadSafe
    @AvailableFromJS
    public void setMaximized(boolean b);

    /**
     * @return whether or not Saucer is resizable by the user.
     */
    @ThreadSafe
    @AvailableFromJS
    public boolean isResizable();

    /**
     * Enables (true) or disables (false) the resizing of Saucer.
     */
    @ThreadSafe
    @AvailableFromJS
    public void setResizable(boolean b);

    /**
     * @return whether or not Saucer has decorations (i.e the title bar).
     */
    @ThreadSafe
    @AvailableFromJS
    public boolean hasDecorations();

    /**
     * Enables (true) or disables (false) Saucer's window decorations (i.e the title
     * bar).
     */
    @ThreadSafe
    @AvailableFromJS
    public void showDecorations(boolean b);

    /**
     * @return whether or not Saucer is always on top of every other window.
     */
    @ThreadSafe
    @AvailableFromJS
    public boolean isAlwaysOnTop();

    /**
     * Sets whether or not Saucer is always on top of every other window.
     */
    @ThreadSafe
    @AvailableFromJS
    public void setAlwaysOnTop(boolean b);

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
     * @return the title of the Saucer window.
     */
    @ThreadSafe
    @AvailableFromJS
    public String getTitle();

    /**
     * Sets the title of the Saucer window.
     */
    @ThreadSafe
    @AvailableFromJS
    public void setTitle(@NonNull String title);

    /**
     * @return the size of the Saucer window.
     */
    @ThreadSafe
    @AvailableFromJS
    public SaucerSize getSize();

    /**
     * Sets the size of the Saucer window.
     */
    @ThreadSafe
    @AvailableFromJS
    public void setSize(@NonNull SaucerSize size);

    /**
     * @return the minimum allowed size of the Saucer window.
     */
    @ThreadSafe
    @AvailableFromJS
    public SaucerSize getMinSize();

    /**
     * Sets the minimum allowed size of the Saucer window.
     */
    @ThreadSafe
    @AvailableFromJS
    public void setMinSize(@NonNull SaucerSize size);

    /**
     * @return the maximum allowed size of the Saucer window.
     */
    @ThreadSafe
    @AvailableFromJS
    public SaucerSize getMaxSize();

    /**
     * Sets the maximum allowed size of the Saucer window.
     */
    @ThreadSafe
    @AvailableFromJS
    public void setMaxSize(@NonNull SaucerSize size);

    /**
     * Hides Saucer, this causes the window to disappear from the taskbar and the
     * user will no longer be able to view the app no matter what they do.
     * 
     * @see #show()
     */
    @ThreadSafe
    @AvailableFromJS
    public void hide();

    /**
     * Unhides Saucer.
     * 
     * @see #hide()
     */
    @ThreadSafe
    @AvailableFromJS
    public void show();

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
