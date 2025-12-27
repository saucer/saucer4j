package app.saucer.ntv;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;

import app.saucer.ntv.ntv_stash.saucer_stash;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerType;
import app.saucer.ntv.util.size_t;

public interface ntv_icon extends Library {
    public static final ntv_icon N = SaucerNativeLoader.load(ntv_icon.class);

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

    public boolean saucer_icon_empty(saucer_icon arg0);

    public saucer_stash saucer_icon_data(saucer_icon arg0);

    public void saucer_icon_save(saucer_icon arg0, String arg1);

    public void saucer_icon_free(saucer_icon arg0);

    public saucer_icon saucer_icon_copy(saucer_icon arg0);

    public saucer_icon saucer_icon_new_from_file(String arg0, IntByReference error);

    public saucer_icon saucer_icon_new_from_stash(saucer_stash arg0, IntByReference error);

    /**
     * @note Please refer to the documentation in `application.h` on how to use this
     *       function.
     */
    public void saucer_icon_native(saucer_icon arg0, size_t arg1, Callback arg2, size_t.ByReference arg3);

}
