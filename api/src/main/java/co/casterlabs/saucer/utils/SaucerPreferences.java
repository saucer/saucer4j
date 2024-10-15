package co.casterlabs.saucer.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer._impl.c.NoFree;
import co.casterlabs.saucer._impl.c.SaucerPointerType;
import co.casterlabs.saucer._impl.c._SaucerMemory;
import co.casterlabs.saucer.documentation.InternalUseOnly;
import lombok.NonNull;

@SuppressWarnings("deprecation")
public final class SaucerPreferences extends SaucerPointerType<SaucerPreferences> {
    private static final _Native N = _SaucerNative.load(_Native.class);

    @Deprecated
    @InternalUseOnly
    public SaucerPreferences() {
        // NOOP constructor for JNA.
    }

    @Override
    protected SaucerPreferences newInstanceForJNA() {
        return new SaucerPreferences();
    }

    @Override
    protected void free() {
        N.saucer_preferences_free(this);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public static SaucerPreferences create() {
        SaucerPreferences preferences = N.saucer_preferences_new(SaucerApp.ptr());
        try {
            File codeSource = new File(SaucerPreferences.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            preferences.storagePath(new File(codeSource.getParentFile(), ".saucer").toPath());
        } catch (URISyntaxException ignored) {}
        return preferences;
    }

    /**
     * Allows cookies to be persisted between app runs.
     * 
     * @return this instance, for chaining.
     */
    public SaucerPreferences persistientCookies(boolean enabled) {
        N.saucer_preferences_set_persistent_cookies(this, enabled);
        return this;
    }

    /**
     * Enables GPU-accelerated rendering, which may increase performance for some
     * applications.
     * 
     * @return this instance, for chaining.
     */
    public SaucerPreferences hardwareAcceleration(boolean enabled) {
        N.saucer_preferences_set_hardware_acceleration(this, enabled);
        return this;
    }

    /**
     * Adds a launch flag to the webview.
     * 
     * @return this instance, for chaining.
     */
    public SaucerPreferences addBrowserFlag(@NonNull String flag) {
        N.saucer_preferences_add_browser_flag(this, _SaucerMemory.alloc(flag));
        return this;
    }

    /**
     * Sets the storage path, e.g cookies, localStorage, request cache.
     * 
     * @return this instance, for chaining.
     */
    public SaucerPreferences storagePath(@NonNull Path path) {
        String pathAsString = path.toAbsolutePath().toString();
        N.saucer_preferences_set_storage_path(this, _SaucerMemory.alloc(pathAsString));
        return this;
    }

    // https://github.com/saucer/bindings/blob/main/include/saucer/preferences.h
    static interface _Native extends Library {

        SaucerPreferences saucer_preferences_new(Pointer $app);

        void saucer_preferences_free(SaucerPreferences instance);

        void saucer_preferences_set_persistent_cookies(SaucerPreferences instance, boolean enabled);

        void saucer_preferences_set_hardware_acceleration(SaucerPreferences instance, boolean enabled);

        void saucer_preferences_add_browser_flag(SaucerPreferences instance, @NoFree Pointer $flag);

        void saucer_preferences_set_storage_path(SaucerPreferences instance, @NoFree Pointer $path);

    }

}
