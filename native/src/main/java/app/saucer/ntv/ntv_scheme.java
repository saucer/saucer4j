package app.saucer.ntv;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import app.saucer.ntv.ntv_stash.saucer_stash;
import app.saucer.ntv.ntv_url.saucer_url;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerType;
import app.saucer.ntv.util.size_t;

public interface ntv_scheme extends Library {
    public static final ntv_scheme N = SaucerNativeLoader.load(ntv_scheme.class);

    public static class saucer_scheme_executor extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_scheme_executor();
        }

        @Override
        protected void free() {
            N.saucer_scheme_executor_free(this);
        }
    }

    public static class saucer_scheme_request extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_scheme_request();
        }

        @Override
        protected void free() {
            N.saucer_scheme_request_free(this);
        }
    }

    public static class saucer_scheme_response extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_scheme_response();
        }

        @Override
        protected void free() {
            N.saucer_scheme_response_free(this);
        }
    }

    public static interface saucer_scheme_handler extends Callback {
        void callback(saucer_scheme_request arg0, saucer_scheme_executor arg1, Pointer arg2);
    }

    public static class saucer_scheme_error {

        public static final int NOT_FOUND = 404;

        public static final int INVALID = 400;

        public static final int DENIED = 401;

        public static final int FAILED = -1;
    };

    public void saucer_scheme_response_free(saucer_scheme_response arg0);

    public saucer_scheme_response saucer_scheme_response_new(saucer_stash arg0, String mime);

    public void saucer_scheme_response_append_header(saucer_scheme_response arg0, String arg1, String arg2);

    public void saucer_scheme_response_set_status(saucer_scheme_response arg0, int arg1);

    public void saucer_scheme_request_free(saucer_scheme_request arg0);

    public saucer_scheme_request saucer_scheme_request_copy(saucer_scheme_request arg0);

    public saucer_url saucer_scheme_request_url(saucer_scheme_request arg0);

    public void saucer_scheme_request_method(saucer_scheme_request arg0, /*string*/byte[] arg1, size_t.ByReference arg2);

    public saucer_stash saucer_scheme_request_content(saucer_scheme_request arg0);

    /**
     * @remark Headers are returned null delimited, e.g. as "Header: Value\0Another
     *         Header: Value"
     */
    public void saucer_scheme_request_headers(saucer_scheme_request arg0, /*string*/byte[] arg1, size_t.ByReference arg2);

    public void saucer_scheme_executor_free(saucer_scheme_executor arg0);

    public saucer_scheme_executor saucer_scheme_executor_copy(saucer_scheme_executor arg0);

    public void saucer_scheme_executor_reject(saucer_scheme_executor arg0, /*saucer_scheme_error*/int arg1);

    public void saucer_scheme_executor_accept(saucer_scheme_executor arg0, saucer_scheme_response arg1);

}
