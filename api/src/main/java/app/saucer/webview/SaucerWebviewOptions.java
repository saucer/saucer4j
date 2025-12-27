package app.saucer.webview;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

import app.saucer.ntv.ntv_webview;
import app.saucer.ntv.ntv_webview.saucer_webview_options;
import app.saucer.ntv.ntv_window.saucer_window;
import app.saucer.ntv.documentation.InternalUseOnly;
import app.saucer.ntv.util.SaucerBoxedType;
import lombok.NonNull;

public final class SaucerWebviewOptions extends SaucerBoxedType<saucer_webview_options> {

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerWebviewOptions(saucer_webview_options $ref) {
        super($ref);
    }

    /**
     * @deprecated Internal use only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerWebviewOptions(@NonNull saucer_window $window) {
        super(ntv_webview.N.saucer_webview_options_new($window));

        try {
            File codeSource = new File(SaucerWebviewOptions.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            this.storagePath(new File(codeSource.getParentFile(), ".saucer").toPath());
        } catch (URISyntaxException ignored) {}
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * Allows cookies to be persisted between app runs.
     * 
     * @return this instance, for chaining.
     */
    public SaucerWebviewOptions persistientCookies(boolean enabled) {
        ntv_webview.N.saucer_webview_options_set_persistent_cookies($ref, enabled);
        return this;
    }

    /**
     * Enables GPU-accelerated rendering, which may increase performance for some
     * applications.
     * 
     * @return this instance, for chaining.
     */
    public SaucerWebviewOptions hardwareAcceleration(boolean enabled) {
        ntv_webview.N.saucer_webview_options_set_hardware_acceleration($ref, enabled);
        return this;
    }

    /**
     * Sets the storage path, e.g cookies, localStorage, request cache.
     * 
     * @return this instance, for chaining.
     */
    public SaucerWebviewOptions storagePath(@NonNull Path path) {
        String pathAsString = path.toAbsolutePath().toString();
        ntv_webview.N.saucer_webview_options_set_storage_path($ref, pathAsString);
        return this;
    }

    /**
     * Sets the storage path, e.g cookies, localStorage, request cache.
     * 
     * @return this instance, for chaining.
     */
    public SaucerWebviewOptions userAgent(@NonNull String userAgent) {
        ntv_webview.N.saucer_webview_options_set_user_agent($ref, userAgent);
        return this;
    }

    /**
     * Adds a launch flag to the webview.
     * 
     * @return this instance, for chaining.
     */
    public SaucerWebviewOptions appendBrowserFlag(@NonNull String flag) {
        ntv_webview.N.saucer_webview_options_append_browser_flag($ref, flag);
        return this;
    }

}
