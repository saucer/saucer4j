package app.saucer.ntv;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerType;
import app.saucer.ntv.util.size_t;

public interface ntv_url extends Library {
    public static final ntv_url N = SaucerNativeLoader.load(ntv_url.class);

    public static class saucer_url extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_url();
        }

        @Override
        protected void free() {
            N.saucer_url_free(this);
        }
    }

    public void saucer_url_free(saucer_url arg0);

    public saucer_url saucer_url_copy(saucer_url arg0);

    /** @note The pointer passed to @param {error} can be null */
    public saucer_url saucer_url_new_parse(String arg0, IntByReference error);

    /** @note The pointer passed to @param {error} can be null */
    public saucer_url saucer_url_new_from(String arg0, IntByReference error);

    public saucer_url saucer_url_new_opts(String scheme, String host, size_t.ByReference port, String path);

    public void saucer_url_string(saucer_url arg0, /*string*/byte[] arg1, size_t.ByReference arg2);

    public void saucer_url_path(saucer_url arg0, /*string*/byte[] arg1, size_t.ByReference arg2);

    public void saucer_url_scheme(saucer_url arg0, /*string*/byte[] arg1, size_t.ByReference arg2);

    /**
     * @note The url might not contain a host. If this is the case, @param {size}
     *       will be set to 0.
     */
    public void saucer_url_host(saucer_url arg0, /*string*/byte[] arg1, size_t.ByReference arg2);

    /**
     * @note The url might not contain a port. If this is the case, @param {port}
     *       will be left unchanged and `false` will be returned.
     */
    public boolean saucer_url_port(saucer_url arg0, size_t.ByReference arg1);

    /**
     * @note The url might not contain a user. If this is the case, @param {size}
     *       will be set to 0.
     */
    public void saucer_url_user(saucer_url arg0, /*string*/byte[] arg1, size_t.ByReference arg2);

    /**
     * @note The url might not contain a password. If this is the case, @param
     *       {size} will be set to 0.
     */
    public void saucer_url_password(saucer_url arg0, /*string*/byte[] arg1, size_t.ByReference arg2);

    /**
     * @note Please refer to the documentation in `application.h` on how to use this
     *       function.
     */
    public void saucer_url_native(saucer_url arg0, size_t arg1, Pointer arg2, size_t.ByReference arg3);

}
