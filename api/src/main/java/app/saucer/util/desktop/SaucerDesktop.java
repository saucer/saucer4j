package app.saucer.util.desktop;

import app.saucer.SaucerApp;
import app.saucer.ntv._desktop;
import lombok.NonNull;

@SuppressWarnings("deprecation")
public final class SaucerDesktop {

    public static void open(@NonNull String uri) {
        _desktop.N.saucer_desktop_open(SaucerApp.ntv_desktop(), uri);
    }

}
