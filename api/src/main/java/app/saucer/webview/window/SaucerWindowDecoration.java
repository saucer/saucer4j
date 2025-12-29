package app.saucer.webview.window;

import app.saucer.ntv.ntv_window.saucer_window_decoration;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SaucerWindowDecoration {
    /**
     * The window is fully undecorated.
     */
    NONE(saucer_window_decoration.NONE),
    /**
     * The window is partially decorated, it draws shadows on top of other windows
     * but has no title bar or borders.
     * 
     * @apiNote This option is not supported on all platforms, and may behave like
     *          {@link #NONE} on some systems.
     */
    PARTIAL(saucer_window_decoration.PARTIAL),
    /**
     * The window is fully decorated with title bar and borders.
     */
    FULL(saucer_window_decoration.FULL);

    static final SaucerWindowDecoration[] LUT = new SaucerWindowDecoration[16];
    static {
        for (SaucerWindowDecoration value : values()) {
            LUT[value.nativeValue] = value;
        }
    }

    final int nativeValue;

}
