package app.saucer.webview.bridge;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import app.saucer.SaucerApp;
import app.saucer.ntv.documentation.InternalUseOnly;
import app.saucer.util.SaucerListenerId;
import app.saucer.webview.SaucerWebview;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.NonNull;

/**
 * @apiNote This class is not thread-safe. You must call it from the main thread
 *          or use {@link SaucerApp#dispatch(Runnable)} or
 *          {@link SaucerApp#dispatch(Supplier)}.
 */
public final class SaucerMessages {
    private Map<SaucerListenerId, Consumer<JsonElement>> listeners = new HashMap<>();
    private final SaucerWebview webview;

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerMessages(SaucerWebview webview) {
        this.webview = webview;
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    synchronized void handle(@Nullable JsonElement data) {
        this.listeners.values().forEach((listener) -> {
            try {
                listener.accept(data);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * Deregisters a listener that you previously registered.
     */
    public synchronized void off(@NonNull SaucerListenerId registrationId) {
        this.listeners.remove(registrationId);
    }

    /**
     * Sends a message to the JavaScript environment.
     * 
     * @param   data the data to send
     * 
     * 
     * 
     * @apiNote      {@link JsonNull#INSTANCE} in Java is `null` in JS. Any non-JSON
     *               type will be automatically marshalled to JSON for you. Rson
     *               will be used to serialize the object, and you will need to
     *               add @JsonClass or @JsonExpose to your code for this to work.
     */
    public void emit(@NonNull Object data) {
        this.webview.bridge.executeJavaScript(
            String.format(
                "window.saucer.messages.__internal(%s);",
                Rson.DEFAULT.toJson(data).toString()
            )
        );
    }

    /**
     * Allows you to listen for all incoming messages.
     * 
     * @return  an registrationId, which you can use when calling
     *          {@link #off(String)}
     * 
     * @apiNote `null` in JS is {@link JsonNull#INSTANCE} in Java.
     */
    public synchronized SaucerListenerId onMessage(@NonNull Consumer<@Nullable JsonElement> callback) {
        SaucerListenerId registrationId = new SaucerListenerId();
        this.listeners.put(registrationId, callback);
        return registrationId;
    }

    /**
     * Allows you to listen for all incoming messages.
     * 
     * @return an registrationId, which you can use when calling
     *         {@link #off(String)}
     */
    public SaucerListenerId onMessage(@NonNull Runnable callback) {
        return this.onMessage((ignored) -> callback.run());
    }

    /**
     * Allows you to listen for all incoming messages with automatic JSON
     * deserialization
     * 
     * @return an registrationId, which you can use when calling
     *         {@link #off(String)}
     */
    public <T> SaucerListenerId onMessage(@NonNull Consumer<@Nullable T> callback, @NonNull TypeToken<T> type) {
        return this.onMessage((data) -> {
            try {
                T typed = Rson.DEFAULT.fromJson(data, type);
                callback.accept(typed);
            } catch (JsonParseException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Allows you to listen for all incoming messages with automatic JSON
     * deserialization.
     * 
     * @apiNote Rson will be used to deserialize the object, and you will need to
     *          add @JsonClass or @JsonExpose to your code for this to work.
     * 
     * @return  an registrationId, which you can use when calling
     *          {@link #off(String)}
     */
    public <T> SaucerListenerId onMessage(@NonNull Consumer<@Nullable T> callback, @NonNull Class<T> type) {
        return this.onMessage(callback, TypeToken.of(type));
    }

}
