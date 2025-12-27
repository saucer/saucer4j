package app.saucer;

import com.sun.jna.ptr.IntByReference;

import app.saucer.ntv.ntv_desktop;
import app.saucer.util.SaucerPosition;
import lombok.NonNull;

@SuppressWarnings("deprecation")
public final class SaucerDesktop {

    public static void open(@NonNull String uri) {
        ntv_desktop.N.saucer_desktop_open(SaucerApp.ntv_desktop(), uri);
    }

    public static SaucerPosition mousePosition() {
        IntByReference xRef = new IntByReference();
        IntByReference yRef = new IntByReference();

        ntv_desktop.N.saucer_desktop_mouse_position(SaucerApp.ntv_desktop(), xRef, yRef);
        return new SaucerPosition(xRef.getValue(), yRef.getValue());
    }

}
