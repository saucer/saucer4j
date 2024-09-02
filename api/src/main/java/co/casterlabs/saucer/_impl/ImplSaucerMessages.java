package co.casterlabs.saucer._impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.saucer.SaucerMessages;
import lombok.NonNull;

@SuppressWarnings("deprecation")
class ImplSaucerMessages implements SaucerMessages {
    private _ImplSaucer saucer;

    private Map<SaucerMessageId, Consumer<JsonElement>> listeners = new HashMap<>();

    ImplSaucerMessages(_ImplSaucer saucer) {
        this.saucer = saucer;
    }

    synchronized void handle(@Nullable JsonElement data) {
        this.listeners.values().forEach((listener) -> {
            try {
                listener.accept(data);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void emit(@NonNull Object data) {
        saucer.bridge.executeJavaScript(
            String.format(
                "window.saucer.messages.__internal(%s);",
                Rson.DEFAULT.toJson(data).toString()
            )
        );
    }

    @Override
    public synchronized void off(@NonNull SaucerMessageId registrationId) {
        this.listeners.remove(registrationId);
    }

    @Override
    public synchronized SaucerMessageId onMessage(@NonNull Consumer<@Nullable JsonElement> callback) {
        SaucerMessageId registrationId = new SaucerMessageId();
        this.listeners.put(registrationId, callback);
        return registrationId;
    }

}
