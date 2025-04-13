package app.saucer;

import java.io.Closeable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import app.saucer.documentation.InternalUseOnly;
import app.saucer.ntv._webview;
import app.saucer.ntv._window;
import app.saucer.ntv._window.SAUCER_WINDOW_EVENT;
import app.saucer.ntv._window.SAUCER_WINDOW_EVENT.WindowClosedCallback;
import app.saucer.ntv._window.saucer_handle;
import app.saucer.ntv.backends.SaucerBackend;
import app.saucer.ntv.backends.SaucerBackendType;
import app.saucer.ntv.documentation.BeforeInit;
import app.saucer.ntv.documentation.NotThreadSafe;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.webview.SaucerWebview;
import app.saucer.webview.bridge.SaucerBridge;
import app.saucer.webview.bridge.SaucerMessages;
import app.saucer.webview.window.SaucerWindow;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * @apiNote This class is not thread-safe. You must call it from the main thread
 *          or use {@link SaucerApp#dispatch(Runnable)} or
 *          {@link SaucerApp#dispatch(Supplier)}.
 */
@NotThreadSafe
public class Saucer extends SaucerBoxedType<saucer_handle> implements Closeable {
    private static final Set<String> customSchemes = new HashSet<>();
    public static final Set<String> registeredSchemes = Collections.unmodifiableSet(customSchemes);

    private static Set<Saucer> instances = new HashSet<>();
    private static boolean alreadyLoaded = false;

    private ExecutorService asyncExecutor = Executors.newSingleThreadExecutor();

    private volatile @Getter boolean isClosed = false;

    private final SaucerWebview webview;
    private final SaucerWindow window;
    private final SaucerBridge bridge;
    private final SaucerMessages messages;

    private WindowClosedCallback shutdownCallback = (_unused) -> {
        this.isClosed = true;
        instances.remove(this);
        this.asyncExecutor.shutdown();
    };

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public Saucer(saucer_handle $ref) {
        super($ref);

        // Keep this object in memory so that it doesn't get free()'d while Saucer is
        // trying to work on it.
        instances.add(this);

        _window.N.saucer_window_on($ref, SAUCER_WINDOW_EVENT.CLOSED, this.shutdownCallback);

        this.webview = new SaucerWebview(this);
        this.window = new SaucerWindow(this);
        this.bridge = new SaucerBridge(this, this.asyncExecutor);
        this.messages = new SaucerMessages(this);
    }

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public saucer_handle ntv() {
        assert !this.isClosed : "This instance has been closed.";
        return $ref;
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    @BeforeInit
    public static void registerCustomScheme(@NonNull String scheme) {
        assert !alreadyLoaded : "You must register all of your custom schemes before calling Saucer.create()";
        assert !customSchemes.contains(scheme) : "Scheme '" + scheme + "' is already registered!";

        _webview.N.saucer_register_scheme(scheme);
        customSchemes.add(scheme);
    }

    public static Saucer create() {
        return create(SaucerPreferences.create());
    }

    @SneakyThrows
    public static Saucer create(@NonNull SaucerPreferences preferences) {
        saucer_handle handle = _webview.N.saucer_new(SaucerBoxedType.ntv(preferences));
        return new Saucer(handle);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public SaucerWebview webview() {
        return this.webview;
    }

    public SaucerWindow window() {
        return this.window;
    }

    public SaucerBridge bridge() {
        return this.bridge;
    }

    public SaucerMessages messages() {
        return this.messages;
    }

    @Override
    public void close() {
        if (this.isClosed) return;
        this.isClosed = true;
        instances.remove(this);
        this.asyncExecutor.shutdown();
        _window.N.saucer_window_close($ref);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public static String getArchTarget() {
        return SaucerBackend.getArchTarget();
    }

    @SneakyThrows
    public static String getSystemTarget() {
        return SaucerBackend.getSystemTarget();
    }

    public static SaucerBackendType getBackendType() {
        return SaucerNativeLoader.getBackend().getType();
    }

}
