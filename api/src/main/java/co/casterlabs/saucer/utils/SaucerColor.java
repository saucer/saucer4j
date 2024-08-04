package co.casterlabs.saucer.utils;

import java.awt.Color;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonSerializer;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.Getter;
import lombok.NonNull;

@Getter
@JsonClass(serializer = SaucerColorSerializer.class)
public final class SaucerColor {
    private int red;
    private int green;
    private int blue;
    private int alpha;

    public SaucerColor() {
        this(255, 255, 255, 255);
    }

    public SaucerColor(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public SaucerColor(int red, int green, int blue, int alpha) {
        this.red(red);
        this.green(green);
        this.blue(blue);
        this.alpha(alpha);
    }

    public SaucerColor(@NonNull Color awt) {
        this(awt.getRed(), awt.getGreen(), awt.getBlue(), awt.getAlpha());
    }

    public int red() {
        return this.red;
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerColor red(int r) {
        assert r >= 0 && r <= 255 : "Red component must be between 0-255.";
        this.red = r;
        return this;
    }

    public int green() {
        return this.green;
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerColor green(int g) {
        assert g >= 0 && g <= 255 : "Green component must be between 0-255.";
        this.green = g;
        return this;
    }

    public int blue() {
        return this.blue;
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerColor blue(int b) {
        assert b >= 0 && b <= 255 : "Blue component must be between 0-255.";
        this.blue = b;
        return this;
    }

    public int alpha() {
        return this.alpha;
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerColor alpha(int a) {
        assert a >= 0 && a <= 255 : "Alpha component must be between 0-255.";
        this.alpha = a;
        return this;
    }

    public Color toAWT() {
        return new Color(this.red, this.blue, this.green, this.alpha);
    }

}

class SaucerColorSerializer implements JsonSerializer<SaucerColor> {

    @Override
    public @Nullable SaucerColor deserialize(@NonNull JsonElement value, @NonNull Class<?> type, @NonNull Rson rson) throws JsonParseException {
        // array: [red,green,blue,alpha]
        // object: {red,green,blue,alpha}

        if (value.isJsonArray()) {
            JsonArray arr = value.getAsArray();
            assert arr.size() < 3 || arr.size() > 4 : new JsonParseException("Array must be either 3 or 4 elements for RGBA.");

            SaucerColor color = new SaucerColor(
                arr.getNumber(0).intValue(),
                arr.getNumber(1).intValue(),
                arr.getNumber(2).intValue()
            );

            if (arr.size() == 4) {
                color.alpha(arr.getNumber(3).intValue());
            }

            return color;
        } else if (value.isJsonObject()) {
            JsonObject obj = value.getAsObject();
            assert obj.containsKey("red") && obj.containsKey("green") && obj.containsKey("blue") : new JsonParseException("Object must have `red`, `green`, `blue`, and optionally `alpha`");

            SaucerColor color = new SaucerColor(
                obj.getNumber("red").intValue(),
                obj.getNumber("green").intValue(),
                obj.getNumber("blue").intValue()
            );

            if (obj.containsKey("alpha")) {
                color.alpha(obj.getNumber("alpha").intValue());
            }

            return color;
        } else {
            throw new JsonParseException("Must be either an array or object!");
        }
    }

    @Override
    public JsonElement serialize(@NonNull Object v, @NonNull Rson rson) {
        SaucerColor value = (SaucerColor) v;
        return new JsonObject()
            .put("red", value.red())
            .put("green", value.green())
            .put("blue", value.blue())
            .put("alpha", value.alpha());
    }

}
