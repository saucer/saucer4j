package app.saucer.nativebindings;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import app.saucer.nativebindings.documentation.RequiresFree;
import app.saucer.nativebindings.util.SaucerNativeLoader;
import app.saucer.nativebindings.util.SaucerPointerReference;
import app.saucer.nativebindings.util.size_t;

public interface _memory extends Library {
    public static final _memory N = SaucerNativeLoader.load(_memory.class);

    public void saucer_memory_free(Pointer data);

    public SaucerPointerReference<@RequiresFree Pointer> saucer_memory_alloc(size_t size);

}
