package app.saucer.nativebindings;

import com.sun.jna.Library;

import app.saucer.nativebindings._app.saucer_application;
import app.saucer.nativebindings.documentation.RequiresFree;
import app.saucer.nativebindings.util.SaucerNativeLoader;
import app.saucer.nativebindings.util.SaucerPointerReference;
import app.saucer.nativebindings.util.SaucerPointerType;

public interface _desktop extends Library {
    public static final _desktop N = SaucerNativeLoader.load(_desktop.class);

    @RequiresFree
    public static class saucer_desktop extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_desktop();
        }

        @Override
        public void free() {
            N.saucer_desktop_free(this);
        }
    }

    public @RequiresFree saucer_desktop saucer_desktop_new(saucer_application app);

    public void saucer_desktop_free(saucer_desktop _instance);

    public void saucer_desktop_open(saucer_desktop _instance, String path);

    @RequiresFree
    public static class saucer_picker_options extends SaucerPointerType {
        @Override
        protected SaucerPointerType newInstance() {
            return new saucer_picker_options();
        }

        @Override
        public void free() {
            N.saucer_picker_options_free(this);
        }
    }

    public @RequiresFree saucer_picker_options saucer_picker_options_new();

    public void saucer_picker_options_free(saucer_picker_options _instance);

    public void saucer_picker_options_set_initial(saucer_picker_options _instance, String path);

    public void saucer_picker_options_add_filter(saucer_picker_options _instance, String filter);

    public @RequiresFree SaucerPointerReference<String> saucer_desktop_pick_file(saucer_desktop _instance, saucer_picker_options options);

    public @RequiresFree SaucerPointerReference<String> saucer_desktop_pick_folder(saucer_desktop _instance, saucer_picker_options options);

    /**
     * @note The returned array will be populated with strings which are themselves
     *       dynamically allocated.
     *
     *       To properly free the returned array you should: - Free all strings
     *       within the array - Free the array itself
     */
    public @RequiresFree SaucerPointerReference<@RequiresFree SaucerPointerReference<String>[]> saucer_desktop_pick_files(saucer_desktop _instance, saucer_picker_options options);

    /**
     * @note The returned array will be populated with strings which are themselves
     *       dynamically allocated.
     *
     *       To properly free the returned array you should: - Free all strings
     *       within the array - Free the array itself
     */

    public @RequiresFree SaucerPointerReference<@RequiresFree SaucerPointerReference<String>[]> saucer_desktop_pick_folders(saucer_desktop _instance, saucer_picker_options options);

}
