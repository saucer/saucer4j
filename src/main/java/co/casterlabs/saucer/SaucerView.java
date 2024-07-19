package co.casterlabs.saucer;

import java.lang.reflect.Constructor;

import co.casterlabs.saucer.documentation.ThreadSafe;
import lombok.NonNull;
import lombok.SneakyThrows;

public interface SaucerView {

    /**
     * @return whether or not the devtools window is open.
     */
    @ThreadSafe
    public boolean isDevtoolsVisible();

    /**
     * @param enabled if true, the devtools window is opened. if false, it is
     *                closed.
     */
    @ThreadSafe
    public void setDevtoolsVisible(boolean enabled);

    /**
     * @return the current URL the webview is navigated to.
     */
    @ThreadSafe
    public String currentUrl();

    /**
     * Navigates the webview to the given URL.
     */
    @ThreadSafe
    public void setUrl(@NonNull String url);

    /**
     * @return whether or not the context menu is enabled.
     */
    @ThreadSafe
    public boolean isContextMenuAllowed();

    /**
     * @param allowed if true.
     */
    @ThreadSafe
    public void setContextMenuAllowed(boolean enabled);

    /**
     * Executes the given JavaScript code in the webview.
     */
    @ThreadSafe
    public void executeJavaScript(@NonNull String scriptToExecute);

    /**
     * Registers a function in the JavaScript environment with the given name.
     */
    public void addFunction(@NonNull String name, @NonNull SaucerFunctionHandler handler);

    public void setBackground(@NonNull SaucerColor color);

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

    /* ------------------------ */
    /* Creation                 */
    /* ------------------------ */

    public static SaucerView create() {
        return create(
            new SaucerOptions() // Defaults are in here.
        );
    }

    @SneakyThrows
    public static SaucerView create(@NonNull SaucerOptions options) {
        // We do this to make sure that the native code is entirely hidden from the
        // user's view.
        Class<?> impl = Class.forName(SaucerView.class.getPackageName() + ".impl.SaucerViewImpl");
        Constructor<?> constructor = impl.getConstructor(SaucerOptions.class);
        constructor.setAccessible(true);

        return (SaucerView) constructor.newInstance(options);
    }

}
