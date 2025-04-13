package app.saucer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import app.saucer.documentation.InternalUseOnly;
import app.saucer.ntv._app;
import app.saucer.ntv._app.saucer_application;
import app.saucer.ntv._app.saucer_post_callback;
import app.saucer.ntv._desktop;
import app.saucer.ntv._desktop.saucer_desktop;
import app.saucer.ntv._options;
import app.saucer.ntv._options.saucer_options;
import lombok.NonNull;
import lombok.SneakyThrows;

public class SaucerApp {
    private static saucer_application $app;
    private static saucer_desktop $desktop;

    private static Thread mainThread;

    @Deprecated
    @InternalUseOnly
    public static saucer_application ntv_app() {
        checkState();
        return $app;
    }

    @Deprecated
    @InternalUseOnly
    public static saucer_desktop ntv_desktop() {
        checkState();
        return $desktop;
    }

    private static void checkState() {
        if ($app == null) {
            throw new IllegalStateException("You must call SaucerApp.initialize() first!");
        }
    }

    private static void checkMainThread() {
        if (Thread.currentThread() != mainThread) {
            throw new IllegalStateException("You must call this method from the thread you initialized the app on!");
        }
    }

    /**
     * Initializes the app.
     * 
     * @apiNote The thread you call this from becomes the main thread.
     */
    public static void initialize(@NonNull String appId) {
        if ($app != null) return; // Silently fail if the app has already been initialized.

        saucer_options $options = _options.N.saucer_options_new(appId);
        try {
//            _options.N.saucer_options_set_argc($options, 0);
//            _options.N.saucer_options_set_argv($options, null);

            $app = _app.N.saucer_application_init($options);
            mainThread = Thread.currentThread();

            $desktop = _desktop.N.saucer_desktop_new($app);
        } finally {
            _options.N.saucer_options_free($options);
        }
    }

    /**
     * @implNote Note that this method blocks until {@link SaucerApp#quit()} is
     *           called or the last window is closed.
     */
    public static void run() {
        checkState();
        checkMainThread();

        _app.N.saucer_application_run($app);

        // After run() returns, the app is done. So we free and set null.
        $desktop.close();
        $app.close();

        $app = null;
        mainThread = null;
    }

    /**
     * This is for custom runloop interop with other libraries.
     */
    @Deprecated
    public static void runOnce() {
        if ($app == null) return; // Silently fail if the app has not been initialized.
        checkMainThread();

        _app.N.saucer_application_run_once($app);
    }

    public static void quit() {
        if ($app == null) return;

        saucer_application old_$instance = $app;
        dispatch(() -> {
            _app.N.saucer_application_quit(old_$instance);
            $desktop.close();
            $app.close();
        });
        $app = null;
        mainThread = null;
    }

    /**
     * Synchronously dispatches the provided task on the main run thread.
     * 
     * @see #dispatch()
     */
    public static void dispatch(@NonNull Runnable task) {
        dispatch(() -> {
            task.run();
            return null;
        });
    }

    /**
     * Synchronously dispatches the provided task on the main run thread, returning
     * the result.
     * 
     * @see #dispatch()
     */
    @SneakyThrows
    public static <T> T dispatch(@NonNull Supplier<T> task) {
        checkState();

        if (Thread.currentThread() == mainThread) {
            return task.get(); // No need to dispatch().
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        saucer_post_callback callback = () -> {
            try {
                future.complete(task.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        };

        _app.N.saucer_application_post($app, callback);

        try {
            return future.join();
        } catch (CompletionException e) {
            throw e.getCause();
        }
    }

}
