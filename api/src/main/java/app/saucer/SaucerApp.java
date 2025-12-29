package app.saucer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import com.sun.jna.ptr.IntByReference;

import app.saucer.bridge.JavascriptGetter;
import app.saucer.bridge.JavascriptObject;
import app.saucer.ntv.ntv_app;
import app.saucer.ntv.ntv_app.saucer_application;
import app.saucer.ntv.ntv_app.saucer_application_options;
import app.saucer.ntv.ntv_app.saucer_post_callback;
import app.saucer.ntv.ntv_app.saucer_screen;
import app.saucer.ntv.ntv_desktop.saucer_desktop;
import app.saucer.ntv.ntv_loop;
import app.saucer.ntv.ntv_loop.saucer_loop;
import app.saucer.ntv.backends.SaucerBackend;
import app.saucer.ntv.backends.SaucerBackendType;
import app.saucer.ntv.documentation.InternalUseOnly;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.size_t;
import app.saucer.util.SaucerScreen;
import lombok.NonNull;
import lombok.SneakyThrows;

@JavascriptObject
@SuppressWarnings("deprecation")
public final class SaucerApp {
    private static saucer_application $app;
    private static saucer_desktop $desktop;
    private static saucer_loop $loop;

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

    private static void cleanup() {
        if ($desktop != null) {
            $desktop.close();
            $desktop = null;
        }
        if ($app != null) {
            $app.close();
            $app = null;
        }
        if ($loop != null) {
            $loop.close();
            $loop = null;
        }
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
    public static void initialize(@NonNull String appId, boolean quitOnLastWindowClose) {
        if ($app != null) return; // Silently fail if the app has already been initialized.

        try (saucer_application_options $options = ntv_app.N.saucer_application_options_new(appId)) {
//            ntv_app.N.saucer_application_options_set_argc($options, 0);
//            ntv_app.N.saucer_application_options_set_argv($options, null);
            ntv_app.N.saucer_application_options_set_quit_on_last_window_closed($options, quitOnLastWindowClose);

            IntByReference error = new IntByReference(0);

            $app = ntv_app.N.saucer_application_new($options, error);

            if (error.getValue() != 0) {
                throw new IllegalStateException("Failed to initialize SaucerApp, error code: " + error.getValue());
            }

            $loop = ntv_loop.N.saucer_loop_new($app);
            mainThread = Thread.currentThread();
        }
    }

    /**
     * @implNote Note that this method blocks until {@link SaucerApp#quit()} is
     *           called or the last window is closed.
     */
    public static void run() {
        checkState();
        checkMainThread();

        ntv_loop.N.saucer_loop_run($loop);

        // After run() returns, the app is done. So we free and set null.
        cleanup();
    }

    /**
     * This is for custom runloop interop with other libraries.
     */
    @Deprecated
    public static void runOnce() {
        if ($app == null) return; // Silently fail if the app has not been initialized.
        checkMainThread();

        ntv_loop.N.saucer_loop_iteration($loop);
    }

    /**
     * Quits the app, stopping the run loop. Note that this *may* leave
     * windows/webviews unresponsive. It is recommended to close all windows before
     * calling this.
     */
    public static void quit() {
        if ($app == null) return;

        dispatch(() -> {
            ntv_loop.N.saucer_loop_quit($loop);
            cleanup();
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
        saucer_post_callback callback = (_unused) -> { // will not be gc'd because future is referenced
            try {
                future.complete(task.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        };

        ntv_app.N.saucer_application_post($app, callback, null);

        try {
            return future.join();
        } catch (CompletionException e) {
            throw e.getCause();
        }
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * @return An array of all available screens.
     * 
     * @see    {@link SaucerScreen}
     */
    @JavascriptGetter("screens")
    public static SaucerScreen[] screens() {
        size_t.ByReference sizeRef = new size_t.ByReference();

        // First call to get the size
        ntv_app.N.saucer_application_screens($app, null, sizeRef);

        // Second call to get the actual string
        saucer_screen[] buffer = new saucer_screen[sizeRef.getValue().intValue()];
        ntv_app.N.saucer_application_screens($app, buffer, sizeRef);

        SaucerScreen[] boxed = new SaucerScreen[buffer.length];
        for (int i = 0; i < buffer.length; i++) {
            boxed[i] = new SaucerScreen(buffer[i], true);
        }
        return boxed;
    }

    /**
     * @return The CPU architecture target of the running Saucer backend.
     */
    @JavascriptGetter("archTarget")
    public static String archTarget() {
        return SaucerBackend.getArchTarget();
    }

    /**
     * @return The OS platform target of the running Saucer backend.
     */
    @SneakyThrows
    @JavascriptGetter("systemTarget")
    public static String systemTarget() {
        return SaucerBackend.getSystemTarget();
    }

    /**
     * @return The type of the running Saucer backend.
     */
    @JavascriptGetter("backendType")
    public static SaucerBackendType backendType() {
        return SaucerNativeLoader.getBackend().getType();
    }

    /**
     * @return The version of the Saucer native library.
     */
    @JavascriptGetter("version")
    public static String version() {
        return ntv_app.N.saucer_version();
    }

}
