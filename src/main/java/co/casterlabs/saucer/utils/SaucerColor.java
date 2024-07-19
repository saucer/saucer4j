package co.casterlabs.saucer.utils;

import java.awt.Color;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonSerializer;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import co.casterlabs.saucer.documentation.InternalUseOnly;
import co.casterlabs.saucer.documentation.PointerType;
import co.casterlabs.saucer.natives._SafePointer;
import co.casterlabs.saucer.natives._SaucerNative;
import lombok.Getter;
import lombok.NonNull;

@Getter
@PointerType
@SuppressWarnings("deprecation")
@JsonClass(serializer = SaucerColorSerializer.class)
public final class SaucerColor extends _SafePointer {
    private static final _Native N = _SaucerNative.load(_Native.class);

    @Deprecated
    @InternalUseOnly
    public SaucerColor(@NonNull Pointer pointer) {
        super.setup(pointer, N::saucer_color_free);
    }

    public SaucerColor() {
        super.setup(N.saucer_color_new(), N::saucer_color_free);
    }

    public SaucerColor(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public SaucerColor(int red, int green, int blue, int alpha) {
        this();
        this.red(red);
        this.green(green);
        this.blue(blue);
        this.alpha(alpha);
    }

    public SaucerColor(@NonNull Color awt) {
        this(awt.getRed(), awt.getGreen(), awt.getBlue(), awt.getAlpha());
    }

    public int red() {
        return N.saucer_color_r(this.p());
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerColor red(int r) {
        assert r >= 0 && r <= 255 : "Red component must be between 0-255.";
        N.saucer_color_set_r(this.p(), r);
        return this;
    }

    public int green() {
        return N.saucer_color_g(this.p());
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerColor green(int g) {
        assert g >= 0 && g <= 255 : "Green component must be between 0-255.";
        N.saucer_color_set_g(this.p(), g);
        return this;
    }

    public int blue() {
        return N.saucer_color_b(this.p());
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerColor blue(int b) {
        assert b >= 0 && b <= 255 : "Blue component must be between 0-255.";
        N.saucer_color_set_b(this.p(), b);
        return this;
    }

    public int alpha() {
        return N.saucer_color_a(this.p());
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerColor alpha(int a) {
        assert a >= 0 && a <= 255 : "Alpha component must be between 0-255.";
        N.saucer_color_set_a(this.p(), a);
        return this;
    }

    public Color toAWT() {
        return new Color(this.red(), this.blue(), this.green(), this.alpha());
    }

    // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/color.h
    static interface _Native extends Library {

        Pointer saucer_color_new();

        void saucer_color_free(Pointer $instance);

        int saucer_color_r(Pointer $instance);

        int saucer_color_g(Pointer $instance);

        int saucer_color_b(Pointer $instance);

        int saucer_color_a(Pointer $instance);

        void saucer_color_set_r(Pointer $instance, int r);

        void saucer_color_set_g(Pointer $instance, int g);

        void saucer_color_set_b(Pointer $instance, int b);

        void saucer_color_set_a(Pointer $instance, int a);

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

            SaucerColor color = new SaucerColor();
            color.red(arr.getNumber(0).intValue());
            color.green(arr.getNumber(1).intValue());
            color.blue(arr.getNumber(2).intValue());

            if (arr.size() == 4) {
                color.alpha(arr.getNumber(3).intValue());
            }

            return color;
        } else if (value.isJsonObject()) {
            JsonObject obj = value.getAsObject();
            assert obj.containsKey("red") && obj.containsKey("green") && obj.containsKey("blue") : new JsonParseException("Object must have  `red`, `green`, `blue`, and optionally `alpha`");

            SaucerColor color = new SaucerColor();
            color.red(obj.getNumber("red").intValue());
            color.green(obj.getNumber("green").intValue());
            color.blue(obj.getNumber("blue").intValue());

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
