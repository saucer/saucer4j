package co.casterlabs.saucer;

import java.util.Collections;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

interface _SaucerNative extends Library {
    static final _SaucerNative N = load();

    static _SaucerNative load() {
        // TODO extract natives.

        return Native.load(
            "saucer-bindings",
            _SaucerNative.class,
            Collections.singletonMap(Library.OPTION_STRING_ENCODING, "UTF-8")
        );
    }

    /* ---------------- */
    /* memory.h         */
    /* ---------------- */

    void saucer_alloc_free(Pointer $ptr);

    Pointer saucer_strdup(String in);

    /* ---------------- */
    /* options.h        */
    /* ---------------- */

    Pointer saucer_options_new();

    void saucer_options_free(Pointer $options);

    void saucer_options_set_persistent_cookies(Pointer $options, boolean enabled);

    void saucer_options_set_hardware_acceleration(Pointer $options, boolean enabled);

    void saucer_options_add_chrome_flag(Pointer $options, String flag);

    void saucer_options_set_storage_path(Pointer $options, String path);

    /* ---------------- */
    /* serializer.h     */
    /* ---------------- */

    /**
     * @implNote Do not inline this. The JVM needs this to always be accessible
     *           otherwise it will garbage collect and ruin our day.
     */
    static interface SerializerCallback extends Callback {

        Pointer callback(Pointer $rawMessage);

    }

    /**
     * @implNote Do not inline this. The JVM needs this to always be accessible
     *           otherwise it will garbage collect and ruin our day.
     */
    static interface SerializerFunction extends Callback {

        /**
         * @param $functionData call
         *                      {@link _SaucerNative#saucer_function_data_get_user_data(Pointer)};
         */
        void callback(Pointer $functionData, Pointer $resolver, Pointer $rejecter);

    }

    Pointer saucer_serializer_new();

    void saucer_serializer_set_js_serializer(Pointer $serializer, String jsSerializer);

    void saucer_serializer_set_parser(Pointer $serializer, SerializerCallback cSerializer);

    Pointer saucer_function_data_new(long id, String name, Pointer userData);

    void saucer_serializer_set_function(Pointer $serializer, SerializerFunction func);

    Pointer saucer_function_data_get_user_data(Pointer $functionData);

    void saucer_serializer_resolve(Pointer $function, String result);

    void saucer_serializer_reject(Pointer $function, String reason);

    /* ---------------- */
    /* smartview.h      */
    /* ---------------- */

    static final int SAUCER_LAUNCH_POLICY_SYNC = 0;
//    static final int SAUCER_LAUNCH_POLICY_ASYNC = 1;

    Pointer saucer_new(Pointer $serializer, Pointer $options);

    void saucer_free(Pointer $view);

    void saucer_add_function(Pointer $view, String name, int saucerLaunchPolicy);

    /* ---------------- */
    /* webview.h        */
    /* ---------------- */

    void saucer_webview_set_dev_tools(Pointer $view, boolean enabled);

    void saucer_webview_set_context_menu(Pointer $view, boolean enabled);

    void saucer_webview_set_url(Pointer $view, String url);

    /* ---------------- */
    /* window.h         */
    /* ---------------- */

    void saucer_window_set_title(Pointer $view, String title);

    void saucer_window_set_size(Pointer $view, int width, int height);

    void saucer_window_show(Pointer $view);

    void saucer_window_run(Pointer $view);

}
