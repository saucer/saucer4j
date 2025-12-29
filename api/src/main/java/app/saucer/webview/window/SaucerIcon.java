package app.saucer.webview.window;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.ptr.IntByReference;

import app.saucer.ntv.ntv_icon;
import app.saucer.ntv.ntv_icon.saucer_icon;
import app.saucer.ntv.ntv_stash;
import app.saucer.ntv.ntv_stash.saucer_stash;
import app.saucer.ntv.documentation.InternalUseOnly;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.size_t;
import lombok.NonNull;
import lombok.ToString;

@ToString
public final class SaucerIcon extends SaucerBoxedType<saucer_icon> {

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerIcon(saucer_icon $ref, boolean autoFree) {
        super($ref, autoFree);
    }

    @Override
    public SaucerIcon clone() {
        saucer_icon cpy = ntv_icon.N.saucer_icon_copy($ref);
        return new SaucerIcon(cpy, true);
    }

    /**
     * @param data PNG
     */
    public static @Nullable SaucerIcon from(@NonNull byte[] data) {
        saucer_stash stash = ntv_stash.N.saucer_stash_new_from(data, new size_t(data.length));
        IntByReference error = new IntByReference(0);

        try {
            saucer_icon icon = ntv_icon.N.saucer_icon_new_from_stash(stash, error);

            if (error.getValue() != 0) {
                throw new IllegalArgumentException("Failed to create SaucerUrl from string, error code: " + error.getValue());
            }

            return new SaucerIcon(icon, true); // do not free the stash, it's used by the icon
        } finally {
            stash.close();
        }
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    @ToString.Include
    private size_t size() {
        try (saucer_stash stash = ntv_icon.N.saucer_icon_data($ref)) {
            return ntv_stash.N.saucer_stash_size(stash);
        }
    }

    /**
     * @return PNG
     */
    public byte[] data() {
        try (saucer_stash stash = ntv_icon.N.saucer_icon_data($ref)) {
            return ntv_stash.N.saucer_stash_data(stash);
        }
    }

    public boolean isEmpty() {
        return ntv_icon.N.saucer_icon_empty($ref);
    }

}
