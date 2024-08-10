package co.casterlabs.saucer._impl.c;

import com.sun.jna.IntegerType;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.documentation.InternalUseOnly;

@SuppressWarnings("deprecation")
@Deprecated
@InternalUseOnly
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

}
