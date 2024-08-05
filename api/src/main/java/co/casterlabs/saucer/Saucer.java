package co.casterlabs.saucer;

import java.util.concurrent.Future;
import java.util.function.Supplier;

import co.casterlabs.commons.platform.LinuxLibC;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.saucer._impl._PackageBridge;
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

    /**
     * Asynchronously dispatches the provided task on the main run thread.
     * 
     * @see     #run()
     * 
     * @apiNote When called from the main run thread, the task will be run
     *          synchronously in-place.
     */
    @SneakyThrows
    @ThreadSafe
    public static void dispatch(@NonNull Runnable task) {
        dispatch(() -> {
            task.run();
            return null;
        });
    }

    /**
     * Asynchronously dispatches the provided task on the main run thread, returning
     * a future so you can retrieve the result.
     * 
     * @see     #run()
     * 
     * @apiNote When called from the main run thread, the task will be run
     *          synchronously in-place.
     */
    @ThreadSafe
    public static <T> Future<T> dispatch(@NonNull Supplier<T> task) {
        return _PackageBridge.dispatch(task);
    }

    /**
     * Synchronously dispatches the provided task on the main run thread.
     * 
     * @see #run()
     */
    @SneakyThrows
    @ThreadSafe
    public static void dispatchSync(@NonNull Runnable task) {
        dispatchSync(() -> {
            task.run();
            return null;
        });
    }

    /**
     * Synchronously dispatches the provided task on the main run thread, returning
     * the result.
     * 
     * @see #run()
     */
    @ThreadSafe
    public static <T> T dispatchSync(@NonNull Supplier<T> task) {
        return _PackageBridge.dispatchSync(task);
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
    public static void run(@NonNull SaucerRunStrategy strategy) {
        _PackageBridge.run(strategy);
    }

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
        return dispatchSync(() -> _PackageBridge.create(options));
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

    public static enum SaucerRunStrategy {
        /**
         * Causes run() to, well, run forever. It will only be stopped with a call to
         * System.exit().
         */
        RUN_FOREVER,

        /**
         * Causes run() to return when the last window is closed.
         */
        RUN_UNTIL_ALL_CLOSED,
    }

}
