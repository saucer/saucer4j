package app.saucer.webview;

import app.saucer.ntv._navigation;
import app.saucer.ntv._navigation.saucer_navigation;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.SaucerPointerReference;

public final class SaucerNavigation extends SaucerBoxedType<saucer_navigation> {

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    public SaucerNavigation(saucer_navigation $ref) {
        super($ref);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public String targetUrl() {
        try (SaucerPointerReference<String> result = _navigation.N.saucer_navigation_url($ref)) {
            return result.asString();
        }
    }

    public NavigationType type() {
        if (_navigation.N.saucer_navigation_new_window($ref)) {
            return NavigationType.NEW_WINDOW;
        } else {
            return NavigationType.REDIRECTION;
        }
    }

    public boolean wasUserInitiated() {
        return _navigation.N.saucer_navigation_user_initiated($ref);
    }

    public static enum NavigationType {
        NEW_WINDOW,
        REDIRECTION
    }

}
