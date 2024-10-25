package app.saucer;

import java.io.Closeable;

import app.saucer._impl._ImplSaucer;
import app.saucer._impl._SaucerNative;
import app.saucer.documentation.AvailableFromJS;
import app.saucer.documentation.NotThreadSafe;
import app.saucer.utils.SaucerPreferences;
import co.casterlabs.commons.platform.LinuxLibC;
import co.casterlabs.commons.platform.Platform;
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
