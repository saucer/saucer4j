package app.saucer.ntv;

import com.sun.jna.Library;

import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerType;

public interface ntv_pdf extends Library {
    public static final ntv_pdf N = SaucerNativeLoader.load(ntv_pdf.class, "pdf");

    public static class saucer_pdf extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_pdf();
        }

        @Override
        protected void free() {
            N.saucer_pdf_free(this);
        }
    }

    public static class saucer_pdf_settings extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_pdf_settings();
        }

        @Override
        protected void free() {
            N.saucer_pdf_settings_free(this);
        }
    }

    public static class saucer_pdf_layout {

        public static final int PORTRAIT = 0;

        public static final int LANDSCAPE = 1;
    };

    public int saucer_pdf_settings_free(saucer_pdf_settings arg0);

    public int saucer_pdf_settings_set_size(saucer_pdf_settings arg0, double w, double h);

    public int saucer_pdf_settings_set_orientation(saucer_pdf_settings arg0, /*saucer_pdf_layout*/int arg1);

    public int saucer_pdf_free(saucer_pdf arg0);

    public int saucer_pdf_save(saucer_pdf arg0, saucer_pdf_settings arg1);

}
