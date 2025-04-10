package app.saucer.nativebindings.util;

import com.sun.jna.Pointer;

import app.saucer.nativebindings._memory;
import app.saucer.nativebindings.documentation.RequiresFree;
import lombok.SneakyThrows;

@RequiresFree
public class SaucerPointerReference<T> extends SaucerPointerType {

    public byte asByte() {
        return $ref.getByte(0);
    }

    public int asInt() {
        return $ref.getInt(0);
    }

    public long asLong() {
        return $ref.getLong(0);
    }

    public float asFloat() {
        return $ref.getFloat(0);
    }

    public double asDouble() {
        return $ref.getDouble(0);
    }

    public short asShort() {
        return $ref.getShort(0);
    }

    public String asString() {
        return $ref.getString(0);
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    @Override
    protected SaucerPointerType newInstance() {
        return new SaucerPointerReference<>();
    }

    @SuppressWarnings({
            "unchecked",
            "deprecation"
    })
    @SneakyThrows
    public <A extends SaucerPointerType> A asType(Class<A> clazz) {
        return (A) clazz.newInstance().fromNative($ref, null);
    }

    @SuppressWarnings({
            "resource",
            "unchecked"
    })
    public <R> SaucerPointerReference<R> referenced() {
        Pointer $ptr = $ref.getPointer(0);
        return (SaucerPointerReference<R>) new SaucerPointerReference<>().fromNative($ptr, null);
    }

    public Pointer self() {
        return $ref;
    }

    @Override
    public void free() {
        _memory.N.saucer_memory_free($ref);
    }

}
