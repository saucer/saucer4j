package app.saucer;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

import app.saucer.documentation.InternalUseOnly;
import app.saucer.ntv._preferences;
import app.saucer.ntv._preferences.saucer_preferences;
import app.saucer.ntv.util.SaucerBoxedType;
import lombok.NonNull;

public final class SaucerPreferences extends SaucerBoxedType<saucer_preferences> {

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerPreferences(saucer_preferences $ref) {
        super($ref);
    }

    @SuppressWarnings("deprecation")
    public static SaucerPreferences create() {
        SaucerPreferences result = new SaucerPreferences(_preferences.N.saucer_preferences_new(SaucerApp.ntv_app()));

        try {
            File codeSource = new File(SaucerPreferences.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            result.storagePath(new File(codeSource.getParentFile(), ".saucer").toPath());
        } catch (URISyntaxException ignored) {}

        return result;
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * Allows cookies to be persisted between app runs.
     * 
     * @return this instance, for chaining.
     */
    public SaucerPreferences persistientCookies(boolean enabled) {
        _preferences.N.saucer_preferences_set_persistent_cookies($ref, enabled);
        return this;
    }

    /**
     * Enables GPU-accelerated rendering, which may increase performance for some
     * applications.
     * 
     * @return this instance, for chaining.
     */
    public SaucerPreferences hardwareAcceleration(boolean enabled) {
        _preferences.N.saucer_preferences_set_hardware_acceleration($ref, enabled);
        return this;
    }

    /**
     * Adds a launch flag to the webview.
     * 
     * @return this instance, for chaining.
     */
    public SaucerPreferences addBrowserFlag(@NonNull String flag) {
        _preferences.N.saucer_preferences_add_browser_flag($ref, flag);
        return this;
    }

    /**
     * Sets the storage path, e.g cookies, localStorage, request cache.
     * 
     * @return this instance, for chaining.
     */
    public SaucerPreferences storagePath(@NonNull Path path) {
        String pathAsString = path.toAbsolutePath().toString();
        _preferences.N.saucer_preferences_set_storage_path($ref, pathAsString);
        return this;
    }

}
