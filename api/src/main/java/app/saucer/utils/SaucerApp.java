package app.saucer.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import app.saucer._impl._SaucerNative;
import app.saucer.documentation.InternalUseOnly;
import lombok.NonNull;
import lombok.SneakyThrows;

@SuppressWarnings("deprecation")
public class SaucerApp {
    private static final _Native N = _SaucerNative.load(_Native.class);
    private static Pointer $instance;
    private static Thread mainThread;

    @Deprecated
    @InternalUseOnly
    public static Pointer ptr() {
        check();
        return $instance;
    }

    private static void check() {
        if ($instance == null) {
            throw new IllegalStateException("You must call SaucerApp.initialize() first!");
        }
    }

    /**
     * Initializes the app. Note that this method blocks until
     * {@link SaucerApp#quit()} is called.
     */
    public static void initialize(@NonNull String appId, @NonNull Runnable continueWith) {
        if ($instance != null) return; // Silently fail if the app has already been initialized.

        $instance = N.saucer_application_acquire(appId);
        mainThread = Thread.currentThread();

        new Thread(() -> SaucerApp.dispatch(continueWith)).start(); // Start executing continueWith ASAP.
        N.saucer_application_run($instance);

        N.saucer_application_free($instance); // After run() returns, the app is done. So we free and set null.
        $instance = null;
        mainThread = null;
    }

    public static void quit() {
        if ($instance == null) return;

        Pointer old_$instance = $instance;
        dispatch(() -> {
            N.saucer_application_quit(old_$instance);
            N.saucer_application_free(old_$instance);
        });
        $instance = null;
        mainThread = null;
    }

    public static boolean isInMainThread() {
        check();
        return Thread.currentThread() == mainThread;
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
        check();

        if (isInMainThread()) {
            return task.get(); // No need to dispatch().
        }

        // Note that the JVM won't garbage collect the callback here until the method
        // returns, which doesn't happen until Saucer is done executing our callback.
        CompletableFuture<T> future = new CompletableFuture<>();

        N.saucer_application_post($instance, () -> {
            try {
                future.complete(task.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        try {
            return future.join();
        } catch (CompletionException e) {
            throw e.getCause();
        }
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    // https://github.com/saucer/bindings/blob/main/include/saucer/app.h
    static interface _Native extends Library {

        Pointer saucer_application_acquire(String id);

        void saucer_application_free(Pointer $instance);

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface SaucerPostCallback extends Callback {
            void callback();
        }

        void saucer_application_post(Pointer $instance, SaucerPostCallback callback);

        void saucer_application_quit(Pointer $instance);

        void saucer_application_run(Pointer $instance);

    }

}
