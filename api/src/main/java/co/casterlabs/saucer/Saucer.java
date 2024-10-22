package co.casterlabs.saucer;

import java.io.Closeable;

import co.casterlabs.commons.platform.LinuxLibC;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.saucer._impl._ImplSaucer;
import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer.documentation.AvailableFromJS;
import co.casterlabs.saucer.documentation.NotThreadSafe;
import co.casterlabs.saucer.utils.SaucerPreferences;
import lombok.NonNull;
import lombok.SneakyThrows;

@SuppressWarnings("deprecation")
public interface Saucer extends Closeable {

    @AvailableFromJS
    public SaucerWebview webview();

    @AvailableFromJS
    public SaucerWindow window();

    @AvailableFromJS
    public SaucerBridge bridge();

    @AvailableFromJS
    public SaucerMessages messages();

    /**
     * Closes the webview, causing {@link #run()} to return. This also frees any
     * resources associated with Saucer.
     */
    @AvailableFromJS
    @Override
    public void close();

    @NotThreadSafe
    public static Saucer create() {
        return create(SaucerPreferences.create());
    }

    @SneakyThrows
    @NotThreadSafe
    public static Saucer create(@NonNull SaucerPreferences preferences) {
        return _ImplSaucer.create(preferences);
    }

    public static void registerCustomScheme(@NonNull String scheme) {
        _ImplSaucer.registerCustomScheme(scheme);
    }

    @AvailableFromJS
    public static String getArchTarget() {
        return Platform.archTarget;
    }

    @AvailableFromJS
    @SneakyThrows
    public static String getSystemTarget() {
        switch (Platform.osDistribution) {
            case LINUX:
                if (LinuxLibC.isGNU()) {
                    return "GNU_Linux";
                } else {
                    return null;
                }

            case WINDOWS_NT:
                return "Windows";

            case MACOS:
                return "macOS";

            default:
                return null;
        }
    }

    @AvailableFromJS
    public static String getBackend() {
        return _SaucerNative.backend;
    }

}
