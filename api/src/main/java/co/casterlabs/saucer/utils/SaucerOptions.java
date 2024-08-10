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
public final class SaucerOptions extends SaucerPointerType<SaucerOptions> {
    private static final _Native N = _SaucerNative.load(_Native.class);

    @Deprecated
    @InternalUseOnly
    public SaucerOptions() {
        // NOOP constructor for JNA.
    }

    @Override
    protected SaucerOptions newInstanceForJNA() {
        return new SaucerOptions();
    }

    @Override
    protected void free() {
        N.saucer_options_free(this);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public static SaucerOptions create() {
        SaucerOptions options = N.saucer_options_new();
        try {
            File codeSource = new File(SaucerOptions.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            options.storagePath(new File(codeSource.getParentFile(), ".saucer").toPath());
        } catch (URISyntaxException ignored) {}
        return options;
    }

    /**
     * Allows cookies to be persisted between app runs.
     * 
     * @return this instance, for chaining.
     */
    public SaucerOptions persistientCookies(boolean enabled) {
        N.saucer_options_set_persistent_cookies(this, enabled);
        return this;
    }

    /**
     * Enables GPU-accelerated rendering, which may increase performance for some
     * applications.
     * 
     * @return this instance, for chaining.
     */
    public SaucerOptions hardwareAcceleration(boolean enabled) {
        N.saucer_options_set_hardware_acceleration(this, enabled);
        return this;
    }

    /**
     * Adds a launch flag to the webview.
     * 
     * @return this instance, for chaining.
     */
    public SaucerOptions addBrowserFlag(@NonNull String flag) {
        N.saucer_options_add_browser_flag(this, _SaucerMemory.alloc(flag));
        return this;
    }

    /**
     * Sets the storage path, e.g cookies, localStorage, request cache.
     * 
     * @return this instance, for chaining.
     */
    public SaucerOptions storagePath(@NonNull Path path) {
        String pathAsString = path.toAbsolutePath().toString();
        N.saucer_options_set_storage_path(this, _SaucerMemory.alloc(pathAsString));
        return this;
    }

    // https://github.com/saucer/bindings/blob/main/include/saucer/options.h
    static interface _Native extends Library {

        SaucerOptions saucer_options_new();

        void saucer_options_free(SaucerOptions instance);

        void saucer_options_set_persistent_cookies(SaucerOptions instance, boolean enabled);

        void saucer_options_set_hardware_acceleration(SaucerOptions instance, boolean enabled);

        void saucer_options_add_browser_flag(SaucerOptions instance, @NoFree Pointer $flag);

        void saucer_options_set_storage_path(SaucerOptions instance, @NoFree Pointer $path);

    }

}
