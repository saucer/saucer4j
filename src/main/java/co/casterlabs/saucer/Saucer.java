package co.casterlabs.saucer;

import java.lang.reflect.Constructor;

import co.casterlabs.saucer.utils.SaucerOptions;
import lombok.NonNull;
import lombok.SneakyThrows;

public interface Saucer extends AutoCloseable {

    public SaucerWebview webview();

    public SaucerWindow window();

    public SaucerBridge bridge();

    public SaucerMessages messages();

    /**
     * Starts the window loop. Note that this blocks until the webview is closed by
     * the user.
     * 
     * @implNote On macOS, the JVM must be started with <a href=
     *           "https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html#:~:text=%2DXstartOnFirstThread">-XstartOnFirstThread</a>
     *           and you must call this method on said thread.
     * 
     */
    public void run();

    /**
     * Closes the webview, causing {@link #run()} to return. This also frees any
     * resources associated with Saucer.
     */
    @Override
    public void close();

    public static Saucer create() {
        return create(
            new SaucerOptions() // Defaults are in here.
        );
    }

    @SneakyThrows
    public static Saucer create(@NonNull SaucerOptions options) {
        // We do this to make sure that the native code is entirely hidden from the
        // user's view.
        Class<?> impl = Class.forName(Saucer.class.getPackageName() + "._impl.ImplSaucer");
        Constructor<?> constructor = impl.getConstructor(SaucerOptions.class);
        constructor.setAccessible(true);

        return (Saucer) constructor.newInstance(options);
    }

}
