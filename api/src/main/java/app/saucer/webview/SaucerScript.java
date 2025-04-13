package app.saucer.webview;

import app.saucer.ntv._script;
import app.saucer.ntv._script.SAUCER_LOAD_TIME;
import app.saucer.ntv._script.SAUCER_WEB_FRAME;
import app.saucer.ntv._script.saucer_script;
import app.saucer.ntv.util.SaucerBoxedType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public final class SaucerScript extends SaucerBoxedType<saucer_script> {

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    public SaucerScript(saucer_script $ref) {
        super($ref);
    }

    public static SaucerScript create(@NonNull String code, @NonNull SaucerLoadTime loadTime) {
        return new SaucerScript(_script.N.saucer_script_new(code, loadTime.val));
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * @return this instance, for chaining.
     */
    public SaucerScript framePolicy(@NonNull SaucerFramePolicy framePolicy) {
        _script.N.saucer_script_set_frame($ref, framePolicy.val);
        return this;
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerScript permanent(boolean permanent) {
        _script.N.saucer_script_set_permanent($ref, permanent);
        return this;
    }

    @RequiredArgsConstructor
    public static enum SaucerLoadTime {
        DOM_CREATION(SAUCER_LOAD_TIME.CREATION),
        DOM_READY(SAUCER_LOAD_TIME.READY),
        ;

        private final int val;
    }

    @RequiredArgsConstructor
    public static enum SaucerFramePolicy {
        TOP(SAUCER_WEB_FRAME.TOP),
        ALL(SAUCER_WEB_FRAME.ALL),
        ;

        private final int val;
    }

}
