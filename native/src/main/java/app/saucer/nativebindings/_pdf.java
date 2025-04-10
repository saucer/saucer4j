package app.saucer.nativebindings;

import com.sun.jna.Library;

import app.saucer.nativebindings._window.saucer_handle;
import app.saucer.nativebindings.documentation.RequiresFree;
import app.saucer.nativebindings.util.SaucerNativeLoader;
import app.saucer.nativebindings.util.SaucerPointerType;

public interface _pdf extends Library {
    public static final _pdf N = SaucerNativeLoader.load(_pdf.class);

    public static class SAUCER_LAYOUT {
        public static final int SAUCER_LAYOUT_PORTRAIT = 0;
        public static final int SAUCER_LAYOUT_LANDSCAPE = 1;
    };

    @RequiresFree
    public static class saucer_print_settings extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_print_settings();
        }

        @Override
        public void free() {
            N.saucer_print_settings_free(this);
        }
    }

    public @RequiresFree saucer_print_settings saucer_print_settings_new();

    public void saucer_print_settings_free(saucer_print_settings _instance);

    public void saucer_print_settings_set_file(saucer_print_settings _instance, String file);

    /**
     * @param orientation {@link SAUCER_LAYOUT}
     */
    public void saucer_print_settings_set_orientation(saucer_print_settings _instance, int orientation);

    public void saucer_print_settings_set_width(saucer_print_settings _instance, double width);

    public void saucer_print_settings_set_height(saucer_print_settings _instance, double height);

    @RequiresFree
    public static class saucer_pdf extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_pdf();
        }

        @Override
        public void free() {
            N.saucer_pdf_free(this);
        }
    }

    public @RequiresFree saucer_pdf saucer_pdf_new(saucer_handle webview);

    public void saucer_pdf_free(saucer_pdf _instance);

    public void saucer_pdf_save(saucer_pdf _instance, saucer_print_settings settings);

}
