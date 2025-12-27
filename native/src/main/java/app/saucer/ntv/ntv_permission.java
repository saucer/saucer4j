package app.saucer.ntv;

import com.sun.jna.Callback;
import com.sun.jna.Library;

import app.saucer.ntv.ntv_url.saucer_url;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerType;
import app.saucer.ntv.util.size_t;

public interface ntv_permission extends Library {
    public static final ntv_permission N = SaucerNativeLoader.load(ntv_permission.class);

    /**
     * @remark Permission-Requests are reference counted. Please make sure to free
     *         all copies to properly release it!
     */
    public static class saucer_permission_request extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_permission_request();
        }

        @Override
        protected void free() {
            N.saucer_permission_request_free(this);
        }
    }

    public static class saucer_permission_type {

        public static final int UNKNOWN = 0;

        public static final int AUDIO_MEDIA = 1;

        public static final int VIDEO_MEDIA = 2;

        public static final int DESKTOP_MEDIA = 4;

        public static final int MOUSE_LOCK = 8;

        public static final int DEVICE_INFO = 16;

        public static final int LOCATION = 32;

        public static final int CLIPBOARD = 64;

        public static final int NOTIFICATION = 128;
    };

    public void saucer_permission_request_free(saucer_permission_request arg0);

    public saucer_permission_request saucer_permission_request_copy(saucer_permission_request arg0);

    public saucer_url saucer_permission_request_url(saucer_permission_request arg0);

    public /*saucer_permission_type*/int saucer_permission_request_type(saucer_permission_request arg0);

    public void saucer_permission_request_accept(saucer_permission_request arg0, boolean arg1);

    /**
     * @note Please refer to the documentation in `application.h` on how to use this
     *       function.
     */
    public void saucer_permission_request_native(saucer_permission_request arg0, size_t arg1, Callback arg2, size_t.ByReference arg3);

}
