package app.saucer.ntv.util;

import com.sun.jna.IntegerType;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class size_t extends IntegerType {
    private static final long serialVersionUID = 1L;

    public size_t() {
        this(0);
    }

    public size_t(long value) {
        super(Native.SIZE_T_SIZE, value, true);
    }

    public static size_t from(Pointer $ptr) {
        long value;
        if (Native.SIZE_T_SIZE == Long.BYTES) {
            value = $ptr.getLong(0);
        } else {
            value = $ptr.getInt(0);
        }
        return new size_t(value);
    }

    @Override
    public String toString() {
        return String.valueOf(this.longValue());
    }

}
