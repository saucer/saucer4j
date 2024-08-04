package co.casterlabs.saucer;

import java.lang.reflect.Constructor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import co.casterlabs.commons.platform.LinuxLibC;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.saucer._impl._SaucerNative;
import co.casterlabs.saucer.documentation.ThreadSafe;
import co.casterlabs.saucer.utils.SaucerOptions;
import lombok.NonNull;
import lombok.SneakyThrows;

@SuppressWarnings("deprecation")
public interface Saucer extends AutoCloseable {

    public SaucerWebview webview();

    public SaucerWindow window();

    public SaucerBridge bridge();

    public SaucerMessages messages();

    @ThreadSafe
    public void dispatch(@NonNull Runnable task);

    @SneakyThrows
    @ThreadSafe
    default <T> T dispatch(@NonNull Supplier<T> task) {
        CompletableFuture<T> future = new CompletableFuture<T>();
        this.dispatch(() -> {
            try {
                T result = task.get();
                future.complete(result);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        try {
            return future.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    /**
     * Starts the window loop. Note that this blocks until the webview is closed by
     * the user.
     * 
     * @implNote On macOS, the JVM must be started with <a href=
     *           "https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html#:~:text=%2DXstartOnFirstThread">-XstartOnFirstThread</a>
     *           and you must call this method on said thread.
     * 
     */
    public void run();

    /**
     * Closes the webview, causing {@link #run()} to return. This also frees any
     * resources associated with Saucer.
     */
    @Override
    public void close();

    public static Saucer create() {
        return create(
            new SaucerOptions() // Defaults are in here.
        );
    }

    @SneakyThrows
    public static Saucer create(@NonNull SaucerOptions options) {
        // We do this to make sure that the native code is entirely hidden from the
        // user's view.
        Class<?> impl = Class.forName(Saucer.class.getPackageName() + "._impl.ImplSaucer");
        Constructor<?> constructor = impl.getConstructor(SaucerOptions.class);
        constructor.setAccessible(true);

        return (Saucer) constructor.newInstance(options);
    }

    @SneakyThrows
    public static void openLinkInSystemBrowser(@NonNull String link) {
        switch (Platform.osDistribution) {
            case MACOS:
                Runtime.getRuntime().exec(new String[] {
                        "open",
                        link
                });
                break;

            case WINDOWS_NT:
                Runtime.getRuntime().exec(new String[] {
                        "rundll32",
                        "url.dll,FileProtocolHandler",
                        link
                });
                break;

            case LINUX:
                Runtime.getRuntime().exec(new String[] {
                        "xdg-open",
                        link
                });
                break;

            default:
                break;
        }
    }

    public static String getArchTarget() {
        return Platform.archTarget;
    }

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

    public static String getBackend() {
        return _SaucerNative.backend;
    }

}
