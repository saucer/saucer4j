package app.saucer.nativebindings;

import com.sun.jna.Library;

import app.saucer.nativebindings.documentation.RequiresFree;
import app.saucer.nativebindings.util.SaucerNativeLoader;
import app.saucer.nativebindings.util.SaucerPointerType;

public interface _script extends Library {
    public static final _script N = SaucerNativeLoader.load(_script.class);

    public static class SAUCER_LOAD_TIME {
        public static final int CREATION = 0;
        public static final int READY = 1;
    };

    public static class SAUCER_WEB_FRAME {
        public static final int TOP = 0;
        public static final int ALL = 1;
    };

    @RequiresFree
    public static class saucer_script extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_script();
        }

        @Override
        public void free() {
            N.saucer_script_free(this);
        }
    }

    /**
     * @param time {@link SAUCER_LOAD_TIME}
     */
    public @RequiresFree saucer_script saucer_script_new(String code, int time);

    public void saucer_script_free(saucer_script _instance);

    /**
     * @param frame {@link SAUCER_WEB_FRAME}
     */
    public void saucer_script_set_frame(saucer_script _instance, int frame);

    /**
     * @param time {@link SAUCER_LOAD_TIME}
     */
    public void saucer_script_set_time(saucer_script _instance, int time);

    public void saucer_script_set_permanent(saucer_script _instance, boolean permanent);

    public void saucer_script_set_code(saucer_script _instance, String code);

}
