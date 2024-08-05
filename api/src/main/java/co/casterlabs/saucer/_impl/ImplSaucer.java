package co.casterlabs.saucer._impl;

import java.util.HashSet;
import java.util.Set;

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

@SuppressWarnings("deprecation")
class ImplSaucer implements Saucer {
    private static final _Native N = _SaucerNative.load(_Native.class);
    static final String CUSTOM_SCHEME = "app";

    private static boolean hasRegisteredCustomScheme = false;

    static Set<ImplSaucer> windows = new HashSet<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
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

    public ImplSaucer(@NonNull SaucerOptions options) {
        if (!hasRegisteredCustomScheme) {
            hasRegisteredCustomScheme = true;
            N.saucer_register_scheme(_SafePointer.allocate(CUSTOM_SCHEME));
        }

        $options = options.toNative();
        $handle = _SafePointer.of(N.saucer_new($options.p()), N::saucer_free);

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

    static void run() {
        N.saucer_window_run();
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

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface DispatchCallback extends Callback {
            void callback();
        }

    }

}
