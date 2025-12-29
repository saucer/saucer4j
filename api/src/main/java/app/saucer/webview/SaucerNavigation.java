package app.saucer.webview;

import app.saucer.ntv.ntv_navigation;
import app.saucer.ntv.ntv_navigation.saucer_navigation;
import app.saucer.ntv.ntv_url.saucer_url;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.util.SaucerUrl;
import lombok.ToString;

@ToString
@SuppressWarnings("deprecation")
public final class SaucerNavigation extends SaucerBoxedType<saucer_navigation> {

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    public SaucerNavigation(saucer_navigation $ref, boolean autoFree) {
        super($ref, autoFree);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    @ToString.Include
    public SaucerUrl targetUrl() {
        saucer_url $url = ntv_navigation.N.saucer_navigation_url($ref);
        return new SaucerUrl($url, false);
    }

    @ToString.Include
    public NavigationType type() {
        if (ntv_navigation.N.saucer_navigation_new_window($ref)) {
            return NavigationType.NEW_WINDOW;
        } else {
            return NavigationType.REDIRECTION;
        }
    }

    @ToString.Include
    public boolean wasUserInitiated() {
        return ntv_navigation.N.saucer_navigation_user_initiated($ref);
    }

    public static enum NavigationType {
        NEW_WINDOW,
        REDIRECTION
    }

}
