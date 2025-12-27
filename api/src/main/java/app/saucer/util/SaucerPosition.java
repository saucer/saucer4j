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
@JsonClass(serializer = SaucerPositionSerializer.class)
public final class SaucerPosition {
    public final int x;
    public final int y;

}

class SaucerPositionSerializer implements JsonSerializer<SaucerPosition> {

    @Override
    public @Nullable SaucerPosition deserialize(@NonNull JsonElement value, @NonNull Class<?> type, @NonNull Rson rson) throws JsonParseException {
        // array syntax: [x,y]
        // object syntax: {x,y}

        if (value.isJsonArray()) {
            JsonArray arr = value.getAsArray();
            assert arr.size() == 2 : new JsonParseException("Array must be 2 elements.");

            return new SaucerPosition(
                arr.getNumber(0).intValue(),
                arr.getNumber(1).intValue()
            );
        }

        if (value.isJsonObject()) {
            JsonObject obj = value.getAsObject();
            assert obj.containsKey("x") && obj.containsKey("y") : new JsonParseException("Object must have `x` and `y`");

            return new SaucerPosition(
                obj.getNumber("x").intValue(),
                obj.getNumber("y").intValue()
            );
        }

        throw new JsonParseException("Must be either an array or object!");
    }

    @Override
    public JsonElement serialize(@NonNull Object v, @NonNull Rson rson) {
        SaucerPosition value = (SaucerPosition) v;
        return new JsonObject()
            .put("x", value.x)
            .put("y", value.y);
    }

}
