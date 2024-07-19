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

    private final ImplSaucerWebview webview;
    private final ImplSaucerWindow window;
    private final @PointerType ImplSaucerBridge bridge;

    public ImplSaucer(@NonNull SaucerOptions options) {
        // These need to be made BEFORE saucer is created.
        $options = options.toNative();
        this.bridge = new ImplSaucerBridge();

        this.setup(N.saucer_new(this.bridge.p(), this.$options.p()), N::saucer_free);

        // These need to be made AFTER saucer is created.
        this.webview = new ImplSaucerWebview(this);
        this.window = new ImplSaucerWindow(this);

        this.window.show(); // Show by default.
        this.window.setMinSize(new SaucerSize(800, 600));
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
        // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/smartview.h
        /* ---------------------------- */

        Pointer saucer_new(Pointer $serializer, Pointer $options);

        void saucer_free(Pointer $saucer);

        /* ---------------------------- */
        // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/window.h
        /* ---------------------------- */

        void saucer_window_close(Pointer $saucer);

        void saucer_window_run(Pointer $saucer);

    }

}
