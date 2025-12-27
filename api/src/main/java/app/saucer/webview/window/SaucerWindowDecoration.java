package app.saucer.webview.window;

import app.saucer.ntv.ntv_window.saucer_window_decoration;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SaucerWindowDecoration {
    NONE(saucer_window_decoration.NONE),
    PARTIAL(saucer_window_decoration.PARTIAL),
    FULL(saucer_window_decoration.FULL);

    static final SaucerWindowDecoration[] LUT = new SaucerWindowDecoration[16];
    static {
        for (SaucerWindowDecoration value : values()) {
            LUT[value.nativeValue] = value;
        }
    }

    final int nativeValue;

}
