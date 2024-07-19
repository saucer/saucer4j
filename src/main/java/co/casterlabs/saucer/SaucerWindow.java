package co.casterlabs.saucer;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.saucer.documentation.AvailableFromJS;
import co.casterlabs.saucer.documentation.ThreadSafe;
import co.casterlabs.saucer.utils.SaucerColor;
import co.casterlabs.saucer.utils.SaucerSize;
import lombok.NonNull;

public interface SaucerWindow {
    // TODO documentation

    @ThreadSafe
    @AvailableFromJS
    public boolean isFocused();

    @ThreadSafe
    @AvailableFromJS
    public void focus();

    @ThreadSafe
    @AvailableFromJS
    public boolean isMinimized();

    @ThreadSafe
    @AvailableFromJS
    public void setMinimized(boolean b);

    @ThreadSafe
    @AvailableFromJS
    public boolean isMaximized();

    @ThreadSafe
    @AvailableFromJS
    public void setMaximized(boolean b);

    @ThreadSafe
    @AvailableFromJS
    public boolean isResizable();

    @ThreadSafe
    @AvailableFromJS
    public void setResizable(boolean b);

    @ThreadSafe
    @AvailableFromJS
    public boolean hasDecorations();

    @ThreadSafe
    @AvailableFromJS
    public void showDecorations(boolean b);

    @ThreadSafe
    @AvailableFromJS
    public boolean isAlwaysOnTop();

    @ThreadSafe
    @AvailableFromJS
    public void setAlwaysOnTop(boolean b);

    @ThreadSafe
    @AvailableFromJS
    public SaucerColor getBackground();

    @ThreadSafe
    @AvailableFromJS
    public void setBackground(@NonNull SaucerColor color);

    @ThreadSafe
    @AvailableFromJS
    public String getTitle();

    @ThreadSafe
    @AvailableFromJS
    public void setTitle(@NonNull String title);

    @ThreadSafe
    @AvailableFromJS
    public SaucerSize getSize();

    @ThreadSafe
    @AvailableFromJS
    public void setSize(@NonNull SaucerSize size);

    @ThreadSafe
    @AvailableFromJS
    public SaucerSize getMinSize();

    @ThreadSafe
    @AvailableFromJS
    public void setMinSize(@NonNull SaucerSize size);

    @ThreadSafe
    @AvailableFromJS
    public SaucerSize getMaxSize();

    @ThreadSafe
    @AvailableFromJS
    public void setMaxSize(@NonNull SaucerSize size);

    @ThreadSafe
    @AvailableFromJS
    public void hide();

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
