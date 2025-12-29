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

/**
 * Represents a rectangle.
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@JsonClass(serializer = SaucerBoundsSerializer.class)
public final class SaucerRectangle {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

}

class SaucerBoundsSerializer implements JsonSerializer<SaucerRectangle> {

    @Override
    public @Nullable SaucerRectangle deserialize(@NonNull JsonElement value, @NonNull Class<?> type, @NonNull Rson rson) throws JsonParseException {
        // array syntax: [x,y,width,height]
        // object syntax: {x,y,width,height}

        if (value.isJsonArray()) {
            JsonArray arr = value.getAsArray();
            assert arr.size() == 4 : new JsonParseException("Array must be 4 elements.");

            return new SaucerRectangle(
                arr.getNumber(0).intValue(),
                arr.getNumber(1).intValue(),
                arr.getNumber(2).intValue(),
                arr.getNumber(3).intValue()
            );
        }

        if (value.isJsonObject()) {
            JsonObject obj = value.getAsObject();
            assert obj.containsKey("x") && obj.containsKey("y") && obj.containsKey("width") && obj.containsKey("height") : new JsonParseException("Object must have `x`, `y`, `width` and `height` properties.");

            return new SaucerRectangle(
                obj.getNumber("x").intValue(),
                obj.getNumber("y").intValue(),
                obj.getNumber("width").intValue(),
                obj.getNumber("height").intValue()
            );
        }

        throw new JsonParseException("Must be either an array or object!");
    }

    @Override
    public JsonElement serialize(@NonNull Object v, @NonNull Rson rson) {
        SaucerRectangle value = (SaucerRectangle) v;
        return new JsonObject()
            .put("x", value.x)
            .put("y", value.y)
            .put("width", value.width)
            .put("height", value.height);
    }

}
