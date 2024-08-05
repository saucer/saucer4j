package co.casterlabs.saucer._impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.SaucerBridge;
import co.casterlabs.saucer.SaucerMessages;
import co.casterlabs.saucer.SaucerWebview;
import co.casterlabs.saucer.SaucerWindow;
import co.casterlabs.saucer._impl.ImplSaucerWindow._Native.WindowVoidCallback;
import co.casterlabs.saucer.utils.SaucerOptions;
import lombok.NonNull;
import lombok.SneakyThrows;

@SuppressWarnings("deprecation")
class ImplSaucer implements Saucer {
    private static final _Native N = _SaucerNative.load(_Native.class);
    static final String CUSTOM_SCHEME = "app";

    private static final ExecutorService DISPATCH_THREADS = Executors.newCachedThreadPool((Runnable r) -> {
        Thread t = new Thread(r);
        t.setName("Saucer - Async Dispatch Thread @" + t.hashCode());
        t.setDaemon(true);
        return t;
    });

    private static volatile Thread runThread = null;
    private static volatile boolean isShuttingDown = false;

    static Set<ImplSaucer> windows = new HashSet<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            isShuttingDown = true;

            for (ImplSaucer window : windows.toArray(new ImplSaucer[0])) {
                window.close();
            }
        }));
    }

    private final _SafePointer $options;
    final _SafePointer $handle;

    final ImplSaucerWebview webview;
    final ImplSaucerWindow window;
    final ImplSaucerBridge bridge;
    final ImplSaucerMessages messages;

    boolean isClosed = false;

    private WindowVoidCallback windowEventClosedCallback = (Pointer $saucer) -> {
        ImplSaucer.windows.remove(ImplSaucer.this);
    };

    static {
        N.saucer_register_scheme(_SafePointer.allocate(CUSTOM_SCHEME));
    }

    public ImplSaucer(@NonNull SaucerOptions options) {
        $options = options.toNative();

        $handle = _SafePointer.of(N.saucer_new(this.$options.p()), N::saucer_free);

        // These need to be initialized AFTER saucer is created.
        this.webview = new ImplSaucerWebview(this);
        this.window = new ImplSaucerWindow(this);
        this.messages = new ImplSaucerMessages(this);
        this.bridge = new ImplSaucerBridge(this); // !MUST BE LAST!

        this.bridge.apply(); // Apply the defaults.

        // Keep this object in memory so that it doesn't get free()'d while Saucer is
        // trying to work on it.
        windows.add(this);
        N.saucer_window_on($handle, _Native.SAUCER_WINDOW_EVENT_CLOSED, this.windowEventClosedCallback);
    }

    @Override
    public SaucerWebview webview() {
        return this.webview;
    }

    @Override
    public SaucerWindow window() {
        return this.window;
    }

    @Override
    public SaucerBridge bridge() {
        return this.bridge;
    }

    @Override
    public SaucerMessages messages() {
        return this.messages;
    }

    @Override
    public void close() {
        N.saucer_window_close($handle);
        this.isClosed = true;
    }

    static <T> Future<T> dispatch(@NonNull Supplier<T> task) {
        final CompletableFuture<T> future = new CompletableFuture<T>();

        if (runThread == Thread.currentThread()) {
            // Avoid dispatch()'ing to Saucer, since we're on the runThread already.
            try {
                T result = task.get();
                future.complete(result);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        } else {
            DISPATCH_THREADS.submit(() -> {
                N.saucer_window_dispatch(() -> {
                    try {
                        T result = task.get();
                        future.complete(result);
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
            });
        }
        return future;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    static <T> T dispatchSync(@NonNull Supplier<T> task) {
        if (runThread == null) {
            throw new IllegalStateException(
                "You tried to run something with dispatchSync() before calling run(). Did you mean to do this instead?\n"
                    + "Saucer.dispatch(() -> { /* code here */ });\n"
                    + "Saucer.run(SaucerRunStrategy.RUN_UNTIL_ALL_CLOSED);"
            );
        }

        if (runThread == Thread.currentThread()) {
            // Avoid dispatch()'ing to Saucer, since we're on the runThread already.
            return task.get();
        }

        Object[] $result = new Object[1]; // Pointers to avoid Java's final requirements.
        Throwable[] $exception = new Throwable[1];

        N.saucer_window_dispatch(() -> {
            try {
                $result[0] = task.get();
            } catch (Throwable t) {
                $exception[0] = t;
            }
        });

        if ($exception[0] != null) {
            throw $exception[0];
        } else {
            return (T) $result[0];
        }
    }

    static void run(@NonNull SaucerRunStrategy strategy) {
        if (runThread != null) {
            throw new IllegalStateException("run() is already active!");
        }

        if (isShuttingDown) {
            return;
        }

        try {
            switch (strategy) {
                case RUN_FOREVER:
                    while (!isShuttingDown) {
                        N.saucer_window_run();
                    }
                    break;

                case RUN_UNTIL_ALL_CLOSED:
                    N.saucer_window_run();
                    return;
            }
        } finally {
            runThread = null;
        }
    }

    static interface _Native extends Library {

        /* ---------------------------- */
        // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/webview.h
        /* ---------------------------- */

        void saucer_register_scheme(_SafePointer $name);

        Pointer saucer_new(Pointer $options);

        void saucer_free(Pointer $saucer);

        /* ---------------------------- */
        // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/window.h
        /* ---------------------------- */

        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WINDOW_EVENT_CLOSED = 2;

        long saucer_window_on(_SafePointer $saucer, int event, Callback callback);

        void saucer_window_close(_SafePointer $saucer);

        void saucer_window_run();

        void saucer_window_dispatch(DispatchCallback callback);

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface DispatchCallback extends Callback {
            void callback();
        }

    }

}
