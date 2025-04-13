package app.saucer.util;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonSerializer;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
@JsonClass(serializer = SaucerSizeSerializer.class)
public class SaucerSize {
    public final int width;
    public final int height;

}

class SaucerSizeSerializer implements JsonSerializer<SaucerSize> {

    @Override
    public @Nullable SaucerSize deserialize(@NonNull JsonElement value, @NonNull Class<?> type, @NonNull Rson rson) throws JsonParseException {
        // array syntax: [width,height]
        // object syntax: {width,height}

        if (value.isJsonArray()) {
            JsonArray arr = value.getAsArray();
            assert arr.size() == 2 : new JsonParseException("Array must be 2 elements.");

            return new SaucerSize(
                arr.getNumber(0).intValue(),
                arr.getNumber(1).intValue()
            );
        }

        if (value.isJsonObject()) {
            JsonObject obj = value.getAsObject();
            assert obj.containsKey("width") && obj.containsKey("height") : new JsonParseException("Object must have `width` and `height`");

            return new SaucerSize(
                obj.getNumber("width").intValue(),
                obj.getNumber("height").intValue()
            );
        }

        throw new JsonParseException("Must be either an array or object!");
    }

    @Override
    public JsonElement serialize(@NonNull Object v, @NonNull Rson rson) {
        SaucerSize value = (SaucerSize) v;
        return new JsonObject()
            .put("width", value.width)
            .put("height", value.height);
    }

}
