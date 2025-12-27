package app.saucer.webview.bridge;

import app.saucer.ntv.ntv_webview.saucer_script_time;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SaucerLoadTime {
    DOM_CREATION(saucer_script_time.CREATION),
    DOM_READY(saucer_script_time.READY);

    final int nativeValue;

}
