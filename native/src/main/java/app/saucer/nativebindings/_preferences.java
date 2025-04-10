package app.saucer.nativebindings;

import com.sun.jna.Library;

import app.saucer.nativebindings._app.saucer_application;
import app.saucer.nativebindings.documentation.RequiresFree;
import app.saucer.nativebindings.util.SaucerNativeLoader;
import app.saucer.nativebindings.util.SaucerPointerType;

public interface _preferences extends Library {
    public static final _preferences N = SaucerNativeLoader.load(_preferences.class);

    @RequiresFree
    public static class saucer_preferences extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_preferences();
        }

        @Override
        public void free() {
            N.saucer_preferences_free(this);
        }
    }

    public @RequiresFree saucer_preferences saucer_preferences_new(saucer_application app);

    public void saucer_preferences_free(saucer_preferences _instance);

    public void saucer_preferences_set_persistent_cookies(saucer_preferences _instance, boolean enabled);

    public void saucer_preferences_set_hardware_acceleration(saucer_preferences _instance, boolean enabled);

    public void saucer_preferences_set_storage_path(saucer_preferences _instance, String path);

    public void saucer_preferences_add_browser_flag(saucer_preferences _instance, String flag);

    public void saucer_preferences_set_user_agent(saucer_preferences _instance, String user_agent);

}
