package app.saucer.ntv;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import app.saucer.ntv.documentation.RequiresFree;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerReference;
import app.saucer.ntv.util.size_t;

public interface _memory extends Library {
    public static final _memory N = SaucerNativeLoader.load(_memory.class);

    public void saucer_memory_free(Pointer data);

    public <T> SaucerPointerReference<@RequiresFree T> saucer_memory_alloc(size_t size);

}
