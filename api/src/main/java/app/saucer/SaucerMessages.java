package app.saucer;

import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import app.saucer.documentation.AvailableFromJS;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.NonNull;

public interface SaucerMessages {

    /**
     * Allows you to listen for all incoming messages.
     * 
     * @return  an registrationId, which you can use when calling
     *          {@link #off(String)}
     * 
     * @apiNote `null` in JS is {@link JsonNull#INSTANCE} in Java.
     */
    @AvailableFromJS
    public SaucerMessageId onMessage(@NonNull Consumer<@Nullable JsonElement> callback);

    /**
     * Allows you to listen for all incoming messages.
     * 
     * @return an registrationId, which you can use when calling
     *         {@link #off(String)}
     */
    @AvailableFromJS
    default SaucerMessageId onMessage(@NonNull Runnable callback) {
        return this.onMessage((ignored) -> callback.run());
    }

    /**
     * Allows you to listen for all incoming messages with automatic JSON
     * deserialization
     * 
     * @return an registrationId, which you can use when calling
     *         {@link #off(String)}
     */
    default <T> SaucerMessageId onMessage(@NonNull Consumer<@Nullable T> callback, @NonNull TypeToken<T> type) {
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
    default <T> SaucerMessageId onMessage(@NonNull Consumer<@Nullable T> callback, @NonNull Class<T> type) {
        return this.onMessage(callback, TypeToken.of(type));
    }

    /**
     * Deregisters a listener that you previously registered.
     */
    @AvailableFromJS
    public void off(@NonNull SaucerMessageId registrationId);

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
    @AvailableFromJS
    public void emit(@NonNull Object data);

    /**
     * This is an opaque type.
     */
    public static class SaucerMessageId {
        private int hash = UUID.randomUUID().toString().hashCode();

        @Override
        public int hashCode() {
            return this.hash;
        }
    }

}
