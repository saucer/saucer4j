package app.saucer.webview.bridge;

import app.saucer.ntv.ntv_webview.saucer_script_time;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SaucerLoadTime {
    /**
     * The time when the DOM is created but before page is loaded.
     */
    DOM_CREATION(saucer_script_time.CREATION),
    /**
     * The time when the page is fully loaded.
     */
    DOM_READY(saucer_script_time.READY);

    final int nativeValue;

}
