package co.casterlabs.saucer;

import java.awt.Color;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.documentation.InternalUseOnly;
import co.casterlabs.saucer.documentation.PointerType;
import co.casterlabs.saucer.impl._SaucerPointer;
import co.casterlabs.saucer.impl._SaucerNative;
import lombok.Getter;
import lombok.NonNull;

@Getter
@PointerType
@SuppressWarnings("deprecation")
public final class SaucerColor extends _SaucerPointer {
    private static final _Native N = _SaucerNative.load(_Native.class);

    @Deprecated
    @InternalUseOnly
    public SaucerColor(@NonNull Pointer pointer) {
        super(pointer, N::saucer_color_free);
    }

    public SaucerColor() {
        super(N.saucer_color_new(), N::saucer_color_free);
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
