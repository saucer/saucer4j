package app.saucer._impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import app.saucer.Saucer;
import app.saucer.SaucerBridge;
import app.saucer.SaucerMessages;
import app.saucer.SaucerWebview;
import app.saucer.SaucerWindow;
import app.saucer._impl.ImplSaucerWindow._Native.WindowVoidCallback;
import app.saucer._impl.c.SaucerPointerType;
import app.saucer._impl.c._SaucerMemory;
import app.saucer.documentation.InternalUseOnly;
import app.saucer.utils.SaucerPreferences;
import lombok.NonNull;

@Deprecated
@InternalUseOnly
public class _ImplSaucer extends SaucerPointerType<_ImplSaucer> implements Saucer {
    private static final _Native N = _SaucerNative.load(_Native.class);

    static final Map<String, Pointer> customSchemes = new HashMap<>();

    private static boolean alreadyLoaded = false;

    private static Set<_ImplSaucer> windows = new HashSet<>();

    final ExecutorService asyncDispatch = Executors.newCachedThreadPool();

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    @Deprecated
    @InternalUseOnly
    public _ImplSaucer() {
        // NOOP constructor for JNA.
    }

    @Override
    protected _ImplSaucer newInstanceForJNA() {
        return new _ImplSaucer();
    }

    @Override
    protected void free() {
        N.saucer_free(this);
        this.preferences = null; // GC will auto-free.
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    @SuppressWarnings("unused")
    private SaucerPreferences preferences; // ref.

    ImplSaucerWebview webview;
    ImplSaucerWindow window;
    ImplSaucerBridge bridge;
    ImplSaucerMessages messages;

    volatile boolean isClosed = false;

    private WindowVoidCallback windowEventClosedCallback = (Pointer $saucer) -> {
        this.isClosed = true;
        _ImplSaucer.windows.remove(_ImplSaucer.this);
        this.asyncDispatch.shutdown();
    };

    public static synchronized _ImplSaucer create(@NonNull SaucerPreferences preferences) {
        alreadyLoaded = true;

        _ImplSaucer saucer = N.saucer_new(preferences);
        saucer.preferences = preferences;

        // These need to be initialized AFTER saucer is created.
        saucer.webview = new ImplSaucerWebview(saucer);
        saucer.window = new ImplSaucerWindow(saucer);
        saucer.messages = new ImplSaucerMessages(saucer);
        saucer.bridge = new ImplSaucerBridge(saucer); // !MUST BE LAST!

        // Keep this object in memory so that it doesn't get free()'d while Saucer is
        // trying to work on it.
        windows.add(saucer);

        N.saucer_window_on(saucer, _Native.SAUCER_WINDOW_EVENT_CLOSED, saucer.windowEventClosedCallback);

        return saucer;
    }

    public static synchronized void registerCustomScheme(@NonNull String scheme) {
        assert !customSchemes.containsKey(scheme) : "Scheme '" + scheme + "' is already registered!";
        assert !alreadyLoaded : "You must register all of your custom schemes before calling Saucer.create()";

        Pointer schemePointer = _SaucerMemory.alloc(scheme);
        N.saucer_register_scheme(schemePointer);

        customSchemes.put(scheme, schemePointer);
    }

    @Override
    public SaucerWebview webview() {
        assert !this.isClosed : "This instance has been closed.";
        return this.webview;
    }

    @Override
    public SaucerWindow window() {
        assert !this.isClosed : "This instance has been closed.";
        return this.window;
    }

    @Override
    public SaucerBridge bridge() {
        assert !this.isClosed : "This instance has been closed.";
        return this.bridge;
    }

    @Override
    public SaucerMessages messages() {
        assert !this.isClosed : "This instance has been closed.";
        return this.messages;
    }

    @Override
    public void close() {
        if (!this.isClosed) {
            N.saucer_window_close(this);
        }
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    static interface _Native extends Library {

        /* ---------------------------- */
        // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/webview.h
        /* ---------------------------- */

        void saucer_register_scheme(Pointer $name);

        _ImplSaucer saucer_new(SaucerPreferences options);

        void saucer_free(_ImplSaucer instance);

        /* ---------------------------- */
        // https://github.com/saucer/saucer/blob/very-experimental/bindings/include/saucer/window.h
        /* ---------------------------- */

        /** Requires {@link WindowVoidCallback} */
        static final int SAUCER_WINDOW_EVENT_CLOSED = 3; // TODO keep this in-sync.

        long saucer_window_on(_ImplSaucer instance, int event, Callback callback);

        void saucer_window_close(_ImplSaucer instance);

    }

}
