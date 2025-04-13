package app.saucer.ntv;

import com.sun.jna.Library;

import app.saucer.ntv._stash.saucer_stash;
import app.saucer.ntv.documentation.RequiresFree;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerReference;
import app.saucer.ntv.util.SaucerPointerType;

public interface _icon extends Library {
    public static final _icon N = SaucerNativeLoader.load(_icon.class);

    @RequiresFree
    public static class saucer_icon extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_icon();
        }

        @Override
        protected void free() {
            N.saucer_icon_free(this);
        }
    }

    public void saucer_icon_free(saucer_icon _instance);

    public boolean saucer_icon_empty(saucer_icon _instance);

    public @RequiresFree saucer_stash saucer_icon_data(saucer_icon _instance);

    public void saucer_icon_save(saucer_icon _instance, String path);

    /**
     * @brief Try to construct an icon from a given file.
     * 
     * @note  The pointer pointed to by @param result will be set to a saucer_icon
     *        in case of success. The returned icon must be free()'d.
     */
    public void saucer_icon_from_file(SaucerPointerReference<@RequiresFree saucer_icon> result, String file);

    /**
     * @brief Try to construct an icon from a given stash (raw bytes).
     * 
     * @note  The pointer pointed to by @param result will be set to a saucer_icon
     *        in case of success. The returned icon must be free()'d.
     */
    public void saucer_icon_from_data(SaucerPointerReference<@RequiresFree saucer_icon> result, saucer_stash stash);

}
