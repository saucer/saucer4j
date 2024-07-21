package co.casterlabs.saucer;

import co.casterlabs.saucer.bridge.JavascriptObject;
import lombok.NonNull;

public interface SaucerBridge {

    /**
     * @param obj your class annotated with {@link JavascriptObject}.
     */
    public void defineObject(@NonNull String name, @NonNull Object obj);

    /**
     * Apply your defined objects to the webview. This causes a page reload.
     */
    public void apply();

}
