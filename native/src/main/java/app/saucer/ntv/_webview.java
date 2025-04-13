package app.saucer.ntv;

import com.sun.jna.Callback;
import com.sun.jna.Library;

import app.saucer.ntv._icon.saucer_icon;
import app.saucer.ntv._navigation.saucer_navigation;
import app.saucer.ntv._preferences.saucer_preferences;
import app.saucer.ntv._scheme.saucer_scheme_handler;
import app.saucer.ntv._script.saucer_script;
import app.saucer.ntv._window.SAUCER_POLICY;
import app.saucer.ntv._window.saucer_handle;
import app.saucer.ntv.documentation.BeforeInit;
import app.saucer.ntv.documentation.NoInline;
import app.saucer.ntv.documentation.RequiresFree;
import app.saucer.ntv.util.SaucerNativeLoader;
import app.saucer.ntv.util.SaucerPointerReference;

public interface _webview extends Library {
    public static final _webview N = SaucerNativeLoader.load(_webview.class);

    public static class SAUCER_WEB_EVENT {
        /* ------------------------ */
        /* ------------------------ */
        /* ------------------------ */

        /** Requires {@link WebviewDomReadyCallback} */
        public static final int DOM_READY = 0;

        @NoInline
        public static interface WebviewDomReadyCallback extends Callback {
            void callback(saucer_handle saucer);
        }

        /* ------------------------ */
        /* ------------------------ */
        /* ------------------------ */

        /** Requires {@link WebviewNavigatedCallback} */
        public static final int NAVIGATED = 1;

        public static interface WebviewNavigatedCallback extends Callback {
            void callback(saucer_handle saucer, String url);
        }

        /* ------------------------ */
        /* ------------------------ */
        /* ------------------------ */

        /** Requires {@link WebviewNavigateCallback} */
        public static final int NAVIGATE = 2;

        @NoInline
        public static interface WebviewNavigateCallback extends Callback {
            /**
             * @return {@link SAUCER_POLICY}
             */
            int callback(saucer_handle saucer, saucer_navigation nav);
        }

        /* ------------------------ */
        /* ------------------------ */
        /* ------------------------ */

        /** Requires {@link WebviewFavIconCallback} */
        public static final int FAVICON = 3;

        @NoInline
        public static interface WebviewFavIconCallback extends Callback {
            void callback(saucer_handle saucer, saucer_icon icon);
        }

        /* ------------------------ */
        /* ------------------------ */
        /* ------------------------ */

        /** Requires {@link WebviewTitleCallback} */
        public static final int TITLE = 4;

        public static interface WebviewTitleCallback extends Callback {
            void callback(saucer_handle saucer, String title);
        }

        /* ------------------------ */
        /* ------------------------ */
        /* ------------------------ */

        /** Requires {@link WebviewLoadCallback} */
        public static final int LOAD = 5;

        public static interface WebviewLoadCallback extends Callback {
            void callback(saucer_handle saucer, SaucerPointerReference<SAUCER_STATE> $state);
        }

    };

    public static class SAUCER_STATE {
        public static final int STARTED = 0;
        public static final int FINISHED = 1;
    };

    public static class SAUCER_LAUNCH {
        public static final int SYNC = 0;
        public static final int ASYNC = 1;
    };

//    struct saucer_embedded_file;
//
//    @RequiresFree  saucer_embedded_file *saucer_embed(saucer_stash *content, const char *mime);
//     void saucer_embed_free(saucer_embedded_file *);

    public @RequiresFree saucer_handle saucer_new(saucer_preferences prefs);

    public void saucer_free(saucer_handle _instance);

    @NoInline
    public static interface saucer_on_message extends Callback {
        boolean callback(String message);
    }

    public void saucer_webview_on_message(saucer_handle _instance, saucer_on_message callback);

    public @RequiresFree saucer_icon saucer_webview_favicon(saucer_handle _instance);

    public @RequiresFree SaucerPointerReference<String> saucer_webview_page_title(saucer_handle _instance);

    public boolean saucer_webview_dev_tools(saucer_handle _instance);

    public @RequiresFree SaucerPointerReference<String> saucer_webview_url(saucer_handle _instance);

    public boolean saucer_webview_context_menu(saucer_handle _instance);

    public void saucer_webview_background(saucer_handle _instance, @RequiresFree SaucerPointerReference<Byte> r, @RequiresFree SaucerPointerReference<Byte> g, @RequiresFree SaucerPointerReference<Byte> b, @RequiresFree SaucerPointerReference<Byte> a);

    public boolean saucer_webview_force_dark_mode(saucer_handle _instance);

    public void saucer_webview_set_dev_tools(saucer_handle _instance, boolean enabled);

    public void saucer_webview_set_context_menu(saucer_handle _instance, boolean enabled);

    public void saucer_webview_set_force_dark_mode(saucer_handle _instance, boolean enabled);

    public void saucer_webview_set_background(saucer_handle _instance, byte r, byte g, byte b, byte a);

    public void saucer_webview_set_file(saucer_handle _instance, String file);

    public void saucer_webview_set_url(saucer_handle _instance, String url);

    public void saucer_webview_back(saucer_handle _instance);

    public void saucer_webview_forward(saucer_handle _instance);

    public void saucer_webview_reload(saucer_handle _instance);

//    void saucer_webview_embed_file(saucer_handle _instance, String name, saucer_embedded_file file, SAUCER_LAUNCH policy);

    public void saucer_webview_serve(saucer_handle _instance, String file);

    public void saucer_webview_clear_scripts(saucer_handle _instance);

    public void saucer_webview_clear_embedded(saucer_handle _instance);

    public void saucer_webview_clear_embedded_file(saucer_handle _instance, String file);

    public void saucer_webview_inject(saucer_handle _instance, saucer_script script);

    public void saucer_webview_execute(saucer_handle _instance, String code);

    /**
     * @param policy {@link SAUCER_LAUNCH}
     */
    public void saucer_webview_handle_scheme(saucer_handle _instance, String name, saucer_scheme_handler handler, int policy);

    public void saucer_webview_remove_scheme(saucer_handle _instance, String name);

    /**
     * @param event {@link SAUCER_WEB_EVENT}
     */
    public void saucer_webview_clear(saucer_handle _instance, int event);

    /**
     * @param event {@link SAUCER_WEB_EVENT}
     */
    public void saucer_webview_remove(saucer_handle _instance, int event, long id);

    /**
     * @param event    {@link SAUCER_WEB_EVENT}
     * @param callback One of the callbacks required by the provided event.
     */
    public void saucer_webview_once(saucer_handle _instance, int event, Callback callback);

    /**
     * @param event    {@link SAUCER_WEB_EVENT}
     * @param callback One of the callbacks required by the provided event.
     */
    public long saucer_webview_on(saucer_handle _instance, int event, Callback callback);

    @BeforeInit
    public void saucer_register_scheme(String name);

}
