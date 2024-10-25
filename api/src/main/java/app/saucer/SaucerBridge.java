package app.saucer;

import app.saucer.bridge.JavascriptObject;
import app.saucer.documentation.AvailableFromJS;
import app.saucer.utils.SaucerScript;
import lombok.NonNull;

public interface SaucerBridge {

    /**
     * Executes the given JavaScript code in the webview.
     */
    @AvailableFromJS
    public void executeJavaScript(@NonNull String scriptToExecute);

    /**
     * @param obj your class annotated with {@link JavascriptObject}.
     */
    public void defineObject(@NonNull String name, @NonNull Object obj);

    public void injectScript(@NonNull SaucerScript script);

    /**
     * Clears all injected scripts and removes defined objects.
     * 
     * Note that permanent scripts will never be removed by this.
     */
    public void clear();

}
