package co.casterlabs.saucer.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer._impl._SafePointer;
import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer.documentation.InternalUseOnly;
import co.casterlabs.saucer.documentation.NoFree;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("deprecation")
public final class SaucerOptions {
    private static final _Native N = _SaucerNative.load(_Native.class);

    private boolean persistientCookies = true;
    private boolean hardwareAcceleration = false;
    private final List<String> chromeFlags = new LinkedList<>();
    private @Nullable Path storagePath;

    public SaucerOptions() {
        try {
            File codeSource = new File(SaucerOptions.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            this.storagePath = new File(codeSource.getParentFile(), ".saucer").toPath();
        } catch (URISyntaxException ignored) {
            this.storagePath = null; // default to $cwd/.saucer
        }
    }

    public List<String> getChromeFlags() {
        // Immutable.
        return Collections.unmodifiableList(this.chromeFlags);
    }

    /**
     * Allows cookies to be persisted between app runs.
     * 
     * @return this instance, for chaining.
     */
    public SaucerOptions persistientCookies(boolean enabled) {
        this.persistientCookies = enabled;
        return this;
    }

    /**
     * Enables GPU-accelerated rendering, which may increase performance for some
     * applications.
     * 
     * @return this instance, for chaining.
     */
    public SaucerOptions hardwareAcceleration(boolean enabled) {
        this.hardwareAcceleration = enabled;
        return this;
    }

    /**
     * Adds a special flag to the Chrome renderer.
     * 
     * @apiNote Only applicable for: Windows, Linux.
     * 
     * @return  this instance, for chaining.
     */
    public SaucerOptions addChromeFlag(@NonNull String flag) {
        this.chromeFlags.add(flag);
        return this;
    }

    /**
     * Sets the storage path, e.g cookies, localStorage, request cache.
     * 
     * @return this instance, for chaining.
     */
    public SaucerOptions storagePath(@NonNull Path path) {
        this.storagePath = path;
        return this;
    }

    @Deprecated
    @InternalUseOnly
    public _SafePointer toNative() {
        _SafePointer $instance = _SafePointer.of(N.saucer_options_new(), N::saucer_options_free);

        N.saucer_options_set_persistent_cookies($instance, this.persistientCookies);
        N.saucer_options_set_hardware_acceleration($instance, this.hardwareAcceleration);

        for (String flag : this.chromeFlags) {
            N.saucer_options_add_chrome_flag($instance, _SaucerNative.allocateUnsafe(flag));
        }

        if (this.storagePath != null) {
            N.saucer_options_set_storage_path($instance, _SaucerNative.allocateUnsafe(this.storagePath.toAbsolutePath().toString()));
        }

        return $instance;
    }

    // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/options.h
    static interface _Native extends Library {

        Pointer saucer_options_new();

        void saucer_options_free(Pointer $instance);

        void saucer_options_set_persistent_cookies(_SafePointer $instance, boolean enabled);

        void saucer_options_set_hardware_acceleration(_SafePointer $instance, boolean enabled);

        void saucer_options_add_chrome_flag(_SafePointer $instance, @NoFree Pointer $flag);

        void saucer_options_set_storage_path(_SafePointer $instance, @NoFree Pointer $path);

    }

}
