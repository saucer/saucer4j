package app.saucer.util;

import java.awt.Color;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonSerializer;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@JsonClass(serializer = SaucerColorSerializer.class)
public final class SaucerColor {
    public final int red;
    public final int green;
    public final int blue;
    public final int alpha;

    public SaucerColor(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public SaucerColor(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public SaucerColor(@NonNull Color awt) {
        this(awt.getRed(), awt.getGreen(), awt.getBlue(), awt.getAlpha());
    }

    public Color toAWT() {
        return new Color(this.red, this.blue, this.green, this.alpha);
    }

}

class SaucerColorSerializer implements JsonSerializer<SaucerColor> {

    @Override
    public @Nullable SaucerColor deserialize(@NonNull JsonElement value, @NonNull Class<?> type, @NonNull Rson rson) throws JsonParseException {
        // array syntax: [red,green,blue,alpha]
        // object syntax: {red,green,blue,alpha}

        if (value.isJsonArray()) {
            JsonArray arr = value.getAsArray();
            assert arr.size() == 3 || arr.size() == 4 : new JsonParseException("Array must be either 3 or 4 elements for RGB or RGBA respectively.");

            if (arr.size() == 4) {
                return new SaucerColor(
                    arr.getNumber(0).intValue(),
                    arr.getNumber(1).intValue(),
                    arr.getNumber(2).intValue(),
                    arr.getNumber(3).intValue()
                );
            } else {
                return new SaucerColor(
                    arr.getNumber(0).intValue(),
                    arr.getNumber(1).intValue(),
                    arr.getNumber(2).intValue()
                );
            }
        }

        if (value.isJsonObject()) {
            JsonObject obj = value.getAsObject();
            assert obj.containsKey("red") && obj.containsKey("green") && obj.containsKey("blue") : new JsonParseException("Object must have `red`, `green`, `blue`, and optionally `alpha`");

            if (obj.containsKey("alpha")) {
                return new SaucerColor(
                    obj.getNumber("red").intValue(),
                    obj.getNumber("green").intValue(),
                    obj.getNumber("blue").intValue(),
                    obj.getNumber("alpha").intValue()
                );
            } else {
                return new SaucerColor(
                    obj.getNumber("red").intValue(),
                    obj.getNumber("green").intValue(),
                    obj.getNumber("blue").intValue()
                );
            }
        }

        throw new JsonParseException("Must be either an array or object!");
    }

    @Override
    public JsonElement serialize(@NonNull Object v, @NonNull Rson rson) {
        SaucerColor value = (SaucerColor) v;
        return new JsonObject()
            .put("red", value.red)
            .put("green", value.green)
            .put("blue", value.blue)
            .put("alpha", value.alpha);
    }

}
