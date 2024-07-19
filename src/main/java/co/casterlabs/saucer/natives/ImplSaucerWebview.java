package co.casterlabs.saucer.natives;

import org.jetbrains.annotations.Nullable;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

import co.casterlabs.saucer.SaucerWebview;
import co.casterlabs.saucer.documentation.PointerType;
import co.casterlabs.saucer.natives.ImplSaucerWebview._Native.URLChangedEventCallback;
import co.casterlabs.saucer.utils.SaucerEmbeddedFiles;
import lombok.NonNull;

@SuppressWarnings("deprecation")
class ImplSaucerWebview implements SaucerWebview {
    private static final _Native N = _SaucerNative.load(_Native.class);

    private final @PointerType ImplSaucer $saucer;

    private @Nullable SaucerWebviewListener eventListener;

    /**
     * Avoid GC'ing too early.
     */
    private SaucerEmbeddedFiles referenceToFiles;

    private VoidCallback webEventLoadFinishedCallback = () -> {
        try {
            if (this.eventListener == null) return;
            this.eventListener.onLoadFinished();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private VoidCallback webEventLoadStartedCallback = () -> {
        try {
            if (this.eventListener == null) return;
            this.eventListener.onLoadStarted();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private URLChangedEventCallback webEventUrlChangedCallback = (newUrl) -> {
        try {
            if (this.eventListener == null) return;
            this.eventListener.onUrlChanged(newUrl);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    private VoidCallback webEventDomReadyCallback = () -> {
        try {
            if (this.eventListener == null) return;
            this.eventListener.onDomReady();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    ImplSaucerWebview(ImplSaucer $saucer) {
        this.$saucer = $saucer;

        // TODO broken.
//        N.saucer_webview_on($saucer.p(), _Native.SAUCER_WEB_EVENT_LOAD_FINISHED, this.webEventLoadFinishedCallback);
//        N.saucer_webview_on($saucer.p(), _Native.SAUCER_WEB_EVENT_LOAD_STARTED, this.webEventLoadStartedCallback);
//        N.saucer_webview_on($saucer.p(), _Native.SAUCER_WEB_EVENT_URL_CHANGED, this.webEventUrlChangedCallback);
//        N.saucer_webview_on($saucer.p(), _Native.SAUCER_WEB_EVENT_DOM_READY, this.webEventDomReadyCallback);
    }

    @Override
    public boolean isDevtoolsVisible() {
        return N.saucer_webview_dev_tools($saucer.p());
    }

    @Override
    public void setDevtoolsVisible(boolean enabled) {
        N.saucer_webview_set_dev_tools($saucer.p(), enabled);
    }

    @Override
    public String currentUrl() {
        return N.saucer_webview_url($saucer.p());
    }

    @Override
    public void setUrl(@NonNull String url) {
        N.saucer_webview_set_url($saucer.p(), url);
    }

    @Override
    public void serve(@NonNull SaucerEmbeddedFiles files, @NonNull String path) {
        N.saucer_webview_embed($saucer.p(), files.p());
        N.saucer_webview_serve($saucer.p(), path);
        this.referenceToFiles = files;
    }

    @Override
    public boolean isContextMenuAllowed() {
        return N.saucer_webview_context_menu($saucer.p());
    }

    @Override
    public void setContextMenuAllowed(boolean enabled) {
        N.saucer_webview_set_context_menu($saucer.p(), enabled);
    }

    @Override
    public void executeJavaScript(@NonNull String scriptToExecute) {
        N.saucer_webview_execute($saucer.p(), scriptToExecute);
    }

    @Override
    public void setListener(@Nullable SaucerWebviewListener listener) {
        this.eventListener = listener;
    }

    // https://github.com/saucer/saucer/blob/c-bindings/bindings/include/saucer/webview.h
    static interface _Native extends Library {
        /** Requires {@link VoidCallback} */
        static final int SAUCER_WEB_EVENT_LOAD_FINISHED = 0;

        /** Requires {@link VoidCallback} */
        static final int SAUCER_WEB_EVENT_LOAD_STARTED = 1;

        /** Requires {@link URLChangedEventCallback} */
        static final int SAUCER_WEB_EVENT_URL_CHANGED = 2;

        /** Requires {@link VoidCallback} */
        static final int SAUCER_WEB_EVENT_DOM_READY = 3;

        boolean saucer_webview_dev_tools(Pointer $saucer);

        String saucer_webview_url(Pointer $saucer);

        boolean saucer_webview_context_menu(Pointer $saucer);

        void saucer_webview_set_dev_tools(Pointer $saucer, boolean setEnabled);

        void saucer_webview_set_context_menu(Pointer $saucer, boolean setEnabled);

        void saucer_webview_set_url(Pointer $saucer, String url);

        void saucer_webview_embed(Pointer $saucer, Pointer $files);

        void saucer_webview_serve(Pointer $saucer, String path);

        void saucer_webview_execute(Pointer $saucer, String script);

        long saucer_webview_on(Pointer $saucer, int saucerWebEvent, Callback callback);

        /**
         * @implNote Do not inline this. The JVM needs this to always be accessible
         *           otherwise it will garbage collect and ruin our day.
         */
        static interface URLChangedEventCallback extends Callback {
            void callback(String newUrl);
        }

    }

}
