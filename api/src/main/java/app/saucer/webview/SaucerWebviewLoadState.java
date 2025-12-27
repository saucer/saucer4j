package app.saucer.webview;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SaucerWebviewLoadState {
    STARTED(0),
    FINISHED(1);

    static final SaucerWebviewLoadState LUT[] = new SaucerWebviewLoadState[16];
    static {
        for (SaucerWebviewLoadState state : values()) {
            LUT[state.nativeValue] = state;
        }
    }

    final int nativeValue;
}
