package app.saucer.webview.window;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Native;

import app.saucer.documentation.InternalUseOnly;
import app.saucer.ntv._icon;
import app.saucer.ntv._icon.saucer_icon;
import app.saucer.ntv._memory;
import app.saucer.ntv._stash;
import app.saucer.ntv._stash.saucer_stash;
import app.saucer.ntv.documentation.RequiresFree;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.SaucerPointerReference;
import app.saucer.ntv.util.size_t;
import lombok.NonNull;

public final class SaucerIcon extends SaucerBoxedType<saucer_icon> {

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerIcon(saucer_icon $ref) {
        super($ref);
    }

    /**
     * @param  data PNG
     * 
     * @return      null if failed
     */
    public static @Nullable SaucerIcon from(@NonNull byte[] data) {
        @RequiresFree
        saucer_stash stash = _stash.N.saucer_stash_from(data, new size_t(data.length));
        try (SaucerPointerReference<saucer_icon> $result = _memory.N.saucer_memory_alloc(new size_t(Native.POINTER_SIZE))) {
            _icon.N.saucer_icon_from_data($result, stash);

            if ($result.referenced().isNull()) {
                return null;
            }

            return new SaucerIcon($result.referenced().as(saucer_icon.class));
        } finally {
            _stash.N.saucer_stash_free(stash);
        }
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * @return PNG
     */
    public byte[] data() {
        @RequiresFree
        saucer_stash stash = _icon.N.saucer_icon_data($ref);
        try {
            return _stash.N.saucer_stash_data(stash);
        } finally {
            _stash.N.saucer_stash_free(stash);
        }
    }

    public boolean isEmpty() {
        return _icon.N.saucer_icon_empty($ref);
    }

}
