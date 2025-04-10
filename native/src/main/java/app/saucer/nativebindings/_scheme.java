package app.saucer.nativebindings;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import app.saucer.nativebindings._stash.saucer_stash;
import app.saucer.nativebindings._window.saucer_handle;
import app.saucer.nativebindings.documentation.NoInline;
import app.saucer.nativebindings.documentation.RequiresFree;
import app.saucer.nativebindings.util.SaucerNativeLoader;
import app.saucer.nativebindings.util.SaucerPointerReference;
import app.saucer.nativebindings.util.SaucerPointerType;
import app.saucer.nativebindings.util.size_t;

public interface _scheme extends Library {
    public static final _scheme N = SaucerNativeLoader.load(_scheme.class);

    public static class SAUCER_SCHEME_ERROR {
        public static final int NOT_FOUND = 0;
        public static final int INVALID = 1;
        public static final int ABORTED = 2;
        public static final int DENIED = 3;
        public static final int FAILED = 4;
    };

    @RequiresFree
    public static class saucer_scheme_response extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_scheme_response();
        }

        /**
         * @apiNote Under normal circumstances you should not need to free the response,
         *          it will be automatically freed by saucer. Use
         *          {@link _scheme#saucer_scheme_response_free} to free the response in
         *          case of an exception.
         */
        @Override
        public void free() {
            N.saucer_scheme_response_free(this);
        }
    }

    public @RequiresFree saucer_scheme_response saucer_scheme_response_new(saucer_stash data, String mime);

    public void saucer_scheme_response_free(saucer_scheme_response _instance);

    public void saucer_scheme_response_set_status(saucer_scheme_response _instance, int status);

    public void saucer_scheme_response_add_header(saucer_scheme_response _instance, String header, String value);

    public static class saucer_scheme_request extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_scheme_request();
        }

        @Override
        public void free() {
            N.saucer_scheme_request_free(this);
        }
    }

    public void saucer_scheme_request_free(saucer_scheme_request _instance);

    public @RequiresFree SaucerPointerReference<String> saucer_scheme_request_url(saucer_scheme_request _instance);

    public @RequiresFree SaucerPointerReference<String> saucer_scheme_request_method(saucer_scheme_request _instance);

    public @RequiresFree saucer_stash saucer_scheme_request_content(saucer_scheme_request _instance);

    /**
     * @note The arrays pointed to by @param headers and @param values will be
     *       populated with strings which are themselves dynamically allocated. Both
     *       arrays will then hold @param count elements.
     *
     *       To properly free the returned arrays you should: - Free all strings
     *       within the headers and values array - Free the array itself
     */
    public void saucer_scheme_request_headers(saucer_scheme_request _instance, @RequiresFree Pointer headers, @RequiresFree Pointer values, size_t count);

    @RequiresFree
    public static class saucer_scheme_executor extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_scheme_executor();
        }

        @Override
        public void free() {
            N.saucer_scheme_executor_free(this);
        }
    }

    public void saucer_scheme_executor_free(saucer_scheme_executor _instance);

    public void saucer_scheme_executor_resolve(saucer_scheme_executor _instance, @RequiresFree saucer_scheme_response response);

    /**
     * @param error {@link SAUCER_SCHEME_ERROR}
     */
    public void saucer_scheme_executor_reject(saucer_scheme_executor _instance, int error);

    @NoInline
    public static interface saucer_scheme_handler extends Callback {
        void callback(saucer_handle saucer, @RequiresFree saucer_scheme_request req, @RequiresFree saucer_scheme_executor exec);
    }

}
