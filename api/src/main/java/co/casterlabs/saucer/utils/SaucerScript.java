package co.casterlabs.saucer.utils;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Library;

import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer._impl.c.SaucerPointerType;
import co.casterlabs.saucer.documentation.InternalUseOnly;
import lombok.NonNull;

@SuppressWarnings("deprecation")
public final class SaucerScript extends SaucerPointerType<SaucerScript> {
    private static final _Native N = _SaucerNative.load(_Native.class);

    @Deprecated
    @InternalUseOnly
    public SaucerScript() {
        // NOOP constructor for JNA.
    }

    @Override
    protected SaucerScript newInstanceForJNA() {
        return new SaucerScript();
    }

    @Override
    protected void free() {
        N.saucer_script_free(this);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public static @Nullable SaucerScript create(@NonNull String code, @NonNull SaucerLoadTime loadTime, @NonNull SaucerFramePolicy framePolicy) {
        SaucerScript script = N.saucer_script_new(code, loadTime.ordinal());

        if (script == null) {
            return null;
        }

        N.saucer_script_set_frame(script, framePolicy.ordinal());

        return script;
    }

    public static @Nullable SaucerScript createPermanent(@NonNull String code, @NonNull SaucerLoadTime loadTime, @NonNull SaucerFramePolicy framePolicy) {
        SaucerScript script = N.saucer_script_new(code, loadTime.ordinal());

        if (script == null) {
            return null;
        }

        N.saucer_script_set_frame(script, framePolicy.ordinal());
        N.saucer_script_set_permanent(script, true);

        return script;
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public static enum SaucerLoadTime {
        DOM_CREATION,
        DOM_READY,
    }

    public static enum SaucerFramePolicy {
        TOP,
        ALL,
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    // https://github.com/saucer/bindings/blob/main/include/saucer/script.h
    static interface _Native extends Library {

        SaucerScript saucer_script_new(String code, int loadTime);

        void saucer_script_free(SaucerScript instance);

        void saucer_script_set_frame(SaucerScript instance, int policy);

        void saucer_script_set_permanent(SaucerScript instance, boolean permanent);

    }

}
