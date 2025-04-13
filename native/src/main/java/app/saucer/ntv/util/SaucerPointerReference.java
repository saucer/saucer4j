package app.saucer.ntv.util;

import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;

import app.saucer.ntv._memory;
import app.saucer.ntv.documentation.DocumentingOnly;
import app.saucer.ntv.documentation.RequiresFree;
import lombok.SneakyThrows;

@RequiresFree
@DocumentingOnly
public class SaucerPointerReference<@DocumentingOnly T> extends SaucerPointerType {

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

    public String[] asStringArray() {
        return $ref.getStringArray(0);
    }

    @SuppressWarnings("unchecked")
    public <A> @RequiresFree SaucerPointerReference<A>[] asArray(int size) {
        Pointer[] pointers = $ref.getPointerArray(0, size);
        SaucerPointerReference<A>[] asRefs = new SaucerPointerReference[pointers.length];

        for (int idx = 0; idx < pointers.length; idx++) {
            asRefs[idx] = of(pointers[idx]);
        }

        return asRefs;
    }

    @SuppressWarnings("unchecked")
    public <A> @RequiresFree SaucerPointerReference<A>[] asArray() {
        Pointer[] pointers = $ref.getPointerArray(0);
        SaucerPointerReference<A>[] asRefs = new SaucerPointerReference[pointers.length];

        for (int idx = 0; idx < pointers.length; idx++) {
            asRefs[idx] = of(pointers[idx]);
        }

        return asRefs;
    }

    public boolean isNull() {
        return $ref == Pointer.NULL;
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    public static <A> SaucerPointerReference<A> of(Pointer $ref) {
        SaucerPointerReference<A> result = new SaucerPointerReference<A>();
        result.$ref = $ref;
        return result;
    }

    @Override
    protected SaucerPointerType newInstance() {
        return new SaucerPointerReference<>();
    }

    @SuppressWarnings({
            "unchecked",
            "deprecation"
    })
    @SneakyThrows
    public <A extends NativeMapped> A as(Class<A> clazz) {
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
