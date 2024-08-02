package co.casterlabs.saucer._impl;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.SaucerBridge;
import co.casterlabs.saucer.SaucerMessages;
import co.casterlabs.saucer.SaucerWebview;
import co.casterlabs.saucer.SaucerWindow;
import co.casterlabs.saucer.utils.SaucerOptions;
import co.casterlabs.saucer.utils.SaucerSize;
import lombok.NonNull;

@SuppressWarnings("deprecation")
class ImplSaucer implements Saucer {
    private static final _Native N = _SaucerNative.load(_Native.class);

    private final _SafePointer $options;
    final _SafePointer $handle;

    final ImplSaucerWebview webview;
    final ImplSaucerWindow window;
    final ImplSaucerBridge bridge;
    final ImplSaucerMessages messages;

    boolean isClosed = false;

    public ImplSaucer(@NonNull SaucerOptions options) {
        $options = options.toNative();

        $handle = _SafePointer.of(N.saucer_new(this.$options.p()), N::saucer_free);

        // These need to be initialized AFTER saucer is created.
        this.webview = new ImplSaucerWebview(this);
        this.window = new ImplSaucerWindow(this);
        this.messages = new ImplSaucerMessages(this);
        this.bridge = new ImplSaucerBridge(this); // !MUST BE LAST!

        this.window.show(); // Show by default.
        this.window.setMinSize(new SaucerSize(800, 600));

        this.bridge.apply(); // Apply by default.
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
    public void dispatchAsync(@NonNull Runnable task) {
        N.saucer_window_dispatch($handle, task::run);
    }

    @Override
    public void run() {
        N.saucer_window_run($handle);
    }

    @Override
    public void close() {
        N.saucer_window_close($handle);
        this.isClosed = true;
    }

    static interface _Native extends Library {

        /* ---------------------------- */
        // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/webview.h
        /* ---------------------------- */

        Pointer saucer_new(Pointer $options);

        void saucer_free(Pointer $saucer);

        /* ---------------------------- */
        // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/window.h
        /* ---------------------------- */

        void saucer_window_close(_SafePointer $saucer);

        void saucer_window_run(_SafePointer $saucer);

        void saucer_window_dispatch(_SafePointer $saucer, WindowDispatchCallback callback);

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface WindowDispatchCallback extends Callback {
            void callback();
        }

    }

}
