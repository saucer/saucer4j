package app.saucer.util.desktop;

import java.io.File;

import org.jetbrains.annotations.Nullable;

import app.saucer.SaucerApp;
import app.saucer.documentation.InternalUseOnly;
import app.saucer.ntv._desktop;
import app.saucer.ntv._desktop.saucer_picker_options;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.SaucerPointerReference;
import lombok.NonNull;

@SuppressWarnings("deprecation")
public class SaucerFilePicker extends SaucerBoxedType<saucer_picker_options> {
    private PickerMode mode = PickerMode.FILES_ONLY;

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerFilePicker(saucer_picker_options $ref) {
        super($ref);
    }

    public static SaucerFilePicker create() {
        return new SaucerFilePicker(_desktop.N.saucer_picker_options_new());
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * The location the file picker should start at.
     * 
     * @return this instance, for chaining.
     */
    public SaucerFilePicker initial(@NonNull File folder) {
        String pathAsString = folder.getAbsolutePath();
        _desktop.N.saucer_picker_options_set_initial($ref, pathAsString);
        return this;
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerFilePicker addFilter(@NonNull String filter) {
        _desktop.N.saucer_picker_options_add_filter($ref, filter);
        return this;
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerFilePicker mode(@NonNull PickerMode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * Selects a single file or folder, depending on the {@link #mode(PickerMode)}
     * you set.
     * 
     * @return null, if the user cancelled the picker operation.
     */
    public @Nullable File pickSingle() {
        SaucerPointerReference<String> pathRef = null;
        try {
            pathRef = SaucerApp.dispatch(() -> {
                if (this.mode == PickerMode.FILES_ONLY) {
                    return _desktop.N.saucer_desktop_pick_file(SaucerApp.ntv_desktop(), $ref);
                } else {
                    return _desktop.N.saucer_desktop_pick_folder(SaucerApp.ntv_desktop(), $ref);
                }
            });

            if (pathRef == null || pathRef.isNull()) {
                return null;
            }

            String pathStr = pathRef.asString();
            return new File(pathStr);
        } finally {
            if (pathRef != null) {
                pathRef.free();
            }
        }
    }

//    /**
//     * Selects multiple files or folders, depending on the {@link #mode(PickerMode)}
//     * you set.
//     * 
//     * @return null, if the user cancelled the picker operation.
//     */
//    public @Nullable File[] pickMultiple() {
//        SaucerPointerReference<SaucerPointerReference<String>[]> pathsRef = null;
//        try {
//            pathRef = SaucerApp.dispatch(() -> {
//                    return _desktop.N.saucer_desktop_pick_files(SaucerApp.ntv_desktop(), $ref);
//                } else {
//                    return _desktop.N.saucer_desktop_pick_folders(SaucerApp.ntv_desktop(), $ref);
//                }
//            });
//
//            if (pathsRef == null || pathsRef.isNull()) {
//                return null;
//            }
//
//            SaucerPointerReference<String>[] paths = pathsRef.asArray();
//            File[] result = new File[paths.length];
//
//            for (int idx = 0; idx < paths.length; idx++) {
//                String pathStr = paths[idx].asString(); // Copy.
//                paths[idx].free();
//
//                result[idx] = new File(pathStr);
//            }
//
//            return result;
//        } finally {
//            if (pathsRef != null) {
//                pathsRef.free();
//            }
//        }
//    }

    public static enum PickerMode {
        /**
         * Only allow the user to select files, not folders.
         */
        FILES_ONLY,

        /**
         * Only allow the user to select folders, not files.
         */
        FOLDERS_ONLY,
    }

}
