package co.casterlabs.saucer;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.saucer.utils.SaucerColor;
import co.casterlabs.saucer.utils.SaucerSize;
import lombok.NonNull;

public interface SaucerWindow {

    public boolean isFocused();

    public void focus();

    public boolean isMinimized();

    public void setMinimized(boolean b);

    public boolean isMaximized();

    public void setMaximized(boolean b);

    public boolean isResizable();

    public void setResizable(boolean b);

    public boolean hasDecorations();

    public void showDecorations(boolean b);

    public boolean isAlwaysOnTop();

    public void setAlwaysOnTop(boolean b);

    public SaucerColor getBackground();

    public void setBackground(@NonNull SaucerColor color);

    public String getTitle();

    public void setTitle(@NonNull String title);

    public SaucerSize getSize();

    public void setSize(@NonNull SaucerSize size);

    public SaucerSize getMinSize();

    public void setMinSize(@NonNull SaucerSize size);

    public SaucerSize getMaxSize();

    public void setMaxSize(@NonNull SaucerSize size);

    public void hide();

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
