package co.casterlabs.saucer.natives;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.SaucerBridge;
import co.casterlabs.saucer.SaucerWebview;
import co.casterlabs.saucer.SaucerWindow;
import co.casterlabs.saucer.documentation.PointerType;
import co.casterlabs.saucer.utils.SaucerOptions;
import co.casterlabs.saucer.utils.SaucerSize;
import lombok.NonNull;

@SuppressWarnings("deprecation")
@PointerType
class ImplSaucer extends _SafePointer implements Saucer {
    private static final _Native N = _SaucerNative.load(_Native.class);

    private final _SafePointer $options;

    final ImplSaucerWebview webview;
    final ImplSaucerWindow window;
    final ImplSaucerBridge bridge;

    public ImplSaucer(@NonNull SaucerOptions options) {
        $options = options.toNative();

        this.setup(N.saucer_new(this.$options.p()), N::saucer_free);

        // These need to be initialized AFTER saucer is created.
        this.webview = new ImplSaucerWebview(this);
        this.window = new ImplSaucerWindow(this);
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
    public void run() {
        N.saucer_window_run(this.p());
    }

    @Override
    public void close() {
        N.saucer_window_close(this.p());
    }

    static interface _Native extends Library {

        /* ---------------------------- */
        // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/webview.h
        /* ---------------------------- */

        Pointer saucer_new(Pointer $options);

        void saucer_free(Pointer $saucer);

        /* ---------------------------- */
        // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/window.h
        /* ---------------------------- */

        void saucer_window_close(Pointer $saucer);

        void saucer_window_run(Pointer $saucer);

    }

}
