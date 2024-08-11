package co.casterlabs.saucer;

import java.awt.Desktop;
import java.io.Closeable;
import java.net.URI;

import co.casterlabs.commons.platform.LinuxLibC;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.saucer._impl._ImplSaucer;
import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer.documentation.AvailableFromJS;
import co.casterlabs.saucer.documentation.NotThreadSafe;
import co.casterlabs.saucer.utils.SaucerOptions;
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
     * Starts the window loop. Note that this blocks until the webview is closed by
     * the user.
     * 
     * @implNote On macOS, the JVM must be started with <a href=
     *           "https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html#:~:text=%2DXstartOnFirstThread">-XstartOnFirstThread</a>
     *           and you must call this method on said thread.
     * 
     */
    @NotThreadSafe
    public static void run() {
        _ImplSaucer.run();
    }

    /**
     * Closes the webview, causing {@link #run()} to return. This also frees any
     * resources associated with Saucer.
     */
    @AvailableFromJS
    @Override
    public void close();

    @NotThreadSafe
    public static Saucer create() {
        return create(SaucerOptions.create());
    }

    @SneakyThrows
    @NotThreadSafe
    public static Saucer create(@NonNull SaucerOptions options) {
        return _ImplSaucer.create(options);
    }

    @AvailableFromJS
    @SneakyThrows
    public static void openLinkInSystemBrowser(@NonNull String link) {
        switch (Platform.osDistribution) {
            case MACOS:
                // We can't use Desktop/AWT while Saucer is running.
                Runtime.getRuntime().exec(new String[] {
                        "open",
                        link
                });
                break;

            case WINDOWS_NT:
                try {
                    Desktop
                        .getDesktop()
                        .browse(URI.create(link));
                } catch (UnsupportedOperationException ignored) {
                    Runtime.getRuntime().exec(new String[] { // Antivirus software may trip the rundll32.
                            "rundll32",
                            "url.dll,FileProtocolHandler",
                            link
                    });
                }
                break;

            case LINUX:
                try {
                    Desktop
                        .getDesktop()
                        .browse(URI.create(link));
                } catch (UnsupportedOperationException ignored) {
                    Runtime.getRuntime().exec(new String[] { // The user might not have xdg-open.
                            "xdg-open",
                            link
                    });
                }
                break;

            default:
                break;
        }
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

            default:
                return null;
        }
    }

    @AvailableFromJS
    public static String getBackend() {
        return _SaucerNative.backend;
    }

}
