package co.casterlabs.saucer._impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.saucer.SaucerMessages;
import co.casterlabs.saucer.documentation.PointerType;
import lombok.NonNull;

@SuppressWarnings("deprecation")
class ImplSaucerMessages implements SaucerMessages {
    private @PointerType ImplSaucer $saucer;

    private Map<String, Consumer<JsonElement>> listeners = new HashMap<>();

    ImplSaucerMessages(ImplSaucer $saucer) {
        this.$saucer = $saucer;
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
        $saucer.webview.executeJavaScript(
            String.format(
                "window.saucer.messages.__internal(%s);",
                Rson.DEFAULT.toJson(data).toString()
            )
        );
    }

    @Override
    public synchronized void off(@NonNull String registrationId) {
        this.listeners.remove(registrationId);
    }

    @Override
    public synchronized String onMessage(@NonNull Consumer<@Nullable JsonElement> callback) {
        String registrationId = UUID.randomUUID().toString();
        this.listeners.put(registrationId, callback);
        return registrationId;
    }

}
