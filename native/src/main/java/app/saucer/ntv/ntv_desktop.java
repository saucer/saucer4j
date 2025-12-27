package app.saucer.ntv;

import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;

import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerType;
import app.saucer.ntv.util.size_t;

public interface ntv_desktop extends Library {
    public static final ntv_desktop N = SaucerNativeLoader.load(ntv_desktop.class, "desktop");

    public static class saucer_desktop extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_desktop();
        }

        @Override
        protected void free() {
            N.saucer_desktop_free(this);
        }
    }

    public static class saucer_picker_options extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_picker_options();
        }

        @Override
        protected void free() {
            N.saucer_picker_options_free(this);
        }
    }

    public saucer_picker_options saucer_picker_options_new();

    public int saucer_picker_options_free(saucer_picker_options arg0);

    public int saucer_picker_options_set_initial(saucer_picker_options arg0, String arg1);

    /**
     * @remark Expects the filters in the format of: "filter1\0filter2\0filter3\0"
     */
    public int saucer_picker_options_set_filters(saucer_picker_options arg0, String arg1, size_t arg2);

    public int saucer_desktop_free(saucer_desktop arg0);

    public int saucer_desktop_mouse_position(saucer_desktop arg0, IntByReference x, IntByReference y);

    public int saucer_picker_pick_file(saucer_desktop arg0, saucer_picker_options arg1, /*string*/byte[] arg2, size_t.ByReference arg3, IntByReference error);

    public int saucer_picker_pick_folder(saucer_desktop arg0, saucer_picker_options arg1, /*string*/byte[] arg2, size_t.ByReference arg3, IntByReference error);

    public int saucer_picker_pick_files(saucer_desktop arg0, saucer_picker_options arg1, /*string*/byte[] arg2, size_t.ByReference arg3, IntByReference error);

    public int saucer_picker_save(saucer_desktop arg0, saucer_picker_options arg1, /*string*/byte[] arg2, size_t.ByReference arg3, IntByReference error);

    public int saucer_desktop_open(saucer_desktop arg0, String arg1);

}
