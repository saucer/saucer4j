package app.saucer.ntv;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import app.saucer.ntv._stash.saucer_stash;
import app.saucer.ntv._window.saucer_handle;
import app.saucer.ntv.documentation.NoInline;
import app.saucer.ntv.documentation.RequiresFree;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerReference;
import app.saucer.ntv.util.SaucerPointerType;
import app.saucer.ntv.util.size_t;

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
        protected void free() {
            N.saucer_scheme_response_free(this);
        }
    }

    public @RequiresFree saucer_scheme_response saucer_scheme_response_new(saucer_stash data, String mime);

    /**
     * @apiNote Under normal circumstances this function should not be used. Once a
     *          saucer_response is returned from within a saucer_scheme_handler it
     *          is automatically deleted. You may use this function to free the
     *          response in case of an exception.
     */
    public void saucer_scheme_response_free(saucer_scheme_response _instance);

    public void saucer_scheme_response_set_status(saucer_scheme_response _instance, int status);

    public void saucer_scheme_response_add_header(saucer_scheme_response _instance, String header, String value);

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

    public void saucer_scheme_request_free(saucer_scheme_request _instance);

    public @RequiresFree SaucerPointerReference<String> saucer_scheme_request_url(saucer_scheme_request _instance);

    public @RequiresFree SaucerPointerReference<String> saucer_scheme_request_method(saucer_scheme_request _instance);

    public @RequiresFree saucer_stash saucer_scheme_request_content(saucer_scheme_request _instance);

    /**
     * @note       The arrays pointed to by @param headers and @param values will be
     *             populated with strings which are themselves dynamically
     *             allocated. Both arrays will then hold @param count elements.
     *
     *             To properly free the returned arrays you should: - Free all
     *             strings within the headers and values array - Free the array
     *             itself
     * 
     * @deprecated This is pretty unsafe, use
     *             {@link #saucer_scheme_request_headers(saucer_scheme_request)}
     *             instead which returns a Java map.
     */
    @Deprecated
    public void saucer_scheme_request_headers(saucer_scheme_request _instance, @RequiresFree Pointer headers, @RequiresFree Pointer values, @RequiresFree Pointer count);

    public static Map<String, String> saucer_scheme_request_headers(saucer_scheme_request _instance) {
        // Setup the pointers for receiving the value.
        try (
            SaucerPointerReference<Pointer> $$keys = _memory.N.saucer_memory_alloc(new size_t(Native.POINTER_SIZE));
            SaucerPointerReference<Pointer> $$values = _memory.N.saucer_memory_alloc(new size_t(Native.POINTER_SIZE));
            SaucerPointerReference<Pointer> $count = _memory.N.saucer_memory_alloc(new size_t(Native.SIZE_T_SIZE))) {
            N.saucer_scheme_request_headers(_instance, $$keys.self(), $$values.self(), $count.self());

            // Get their values.
            try (
                SaucerPointerReference<Pointer> $keys = $$keys.referenced();
                SaucerPointerReference<Pointer> $values = $$values.referenced()) {

                size_t count = $count.as(size_t.class);

                // Convert the key/values pointers to arrays.
                SaucerPointerReference<Pointer>[] keys = $keys.asArray(count.intValue());
                SaucerPointerReference<Pointer>[] values = $values.asArray(count.intValue());

                // Convert to map.
                Map<String, String> map = new HashMap<>();
                for (int idx = 0; idx < keys.length; idx++) {
                    try (
                        SaucerPointerReference<Pointer> $key = keys[idx];
                        SaucerPointerReference<Pointer> $value = values[idx]) {
                        map.put($key.asString(), $value.asString());
                    }
                }
                return map;
            }
        }
    }

    @RequiresFree
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
