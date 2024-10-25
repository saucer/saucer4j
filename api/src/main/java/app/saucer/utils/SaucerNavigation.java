package app.saucer.utils;

import com.sun.jna.Library;

import app.saucer._impl._SaucerNative;
import app.saucer._impl.c.SaucerPointerType;
import app.saucer.documentation.InternalUseOnly;

@SuppressWarnings("deprecation")
public final class SaucerNavigation extends SaucerPointerType<SaucerNavigation> {
    private static final _Native N = _SaucerNative.load(_Native.class);

    @Deprecated
    @InternalUseOnly
    public SaucerNavigation() {
        // NOOP constructor for JNA.
    }

    @Override
    protected SaucerNavigation newInstanceForJNA() {
        return new SaucerNavigation();
    }

    @Override
    protected void free() {
        N.saucer_navigation_free(this);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public String getTargetUrl() {
        return N.saucer_navigation_url(this);
    }

    public NavigationType getType() {
        if (N.saucer_navigation_new_window(this)) {
            return NavigationType.NEW_WINDOW;
        } else {
            return NavigationType.REDIRECTION;
        }
    }

    public boolean wasUserInitiated() {
        return N.saucer_navigation_user_initiated(this);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public static enum NavigationType {
        NEW_WINDOW,
        REDIRECTION
    }

    // https://github.com/saucer/bindings/blob/main/include/saucer/icon.h
    static interface _Native extends Library {

        void saucer_navigation_free(SaucerNavigation instance);

        String saucer_navigation_url(SaucerNavigation instance);

        boolean saucer_navigation_new_window(SaucerNavigation instance);

        boolean saucer_navigation_redirection(SaucerNavigation instance);

        boolean saucer_navigation_user_initiated(SaucerNavigation instance);

    }

}
