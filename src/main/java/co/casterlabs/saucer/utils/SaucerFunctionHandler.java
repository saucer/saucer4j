package co.casterlabs.saucer.utils;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import lombok.NonNull;

@FunctionalInterface
public interface SaucerFunctionHandler {

    /**
     * @return null for undefined, JsonNull.INSTANCE for null.
     */
    public @Nullable JsonElement handle(@NonNull JsonArray arguments) throws Throwable;

}
