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

    public static class ByReference extends com.sun.jna.ptr.ByReference {
        public ByReference() {
            this(new size_t(0));
        }

        public ByReference(size_t value) {
            super(Native.SIZE_T_SIZE);
            setValue(value);
        }

        public void setValue(size_t value) {
            if (Native.SIZE_T_SIZE == Long.BYTES) {
                getPointer().setLong(0, value.longValue());
            } else {
                getPointer().setInt(0, value.intValue());
            }
        }

        public size_t getValue() {
            return size_t.from(getPointer());
        }
    }

}
