package app.saucer;

import java.io.File;

import app.saucer.ntv.ntv_desktop;
import app.saucer.ntv.ntv_desktop.saucer_picker_options;
import app.saucer.ntv.documentation.InternalUseOnly;
import app.saucer.ntv.util.SaucerBoxedType;
import app.saucer.ntv.util.size_t;
import lombok.NonNull;

public final class SaucerFilePicker extends SaucerBoxedType<saucer_picker_options> {

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerFilePicker(saucer_picker_options $ref, boolean autoFree) {
        super($ref, autoFree);
    }

    public static SaucerFilePicker create() {
        return new SaucerFilePicker(ntv_desktop.N.saucer_picker_options_new(), true);
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
        ntv_desktop.N.saucer_picker_options_set_initial($ref, pathAsString);
        return this;
    }

    /**
     * @return this instance, for chaining.
     */
    public SaucerFilePicker setFilters(@NonNull String... filters) {
        String joined = String.join("\0", filters);
        ntv_desktop.N.saucer_picker_options_set_filters($ref, joined, new size_t(joined.length()));
        return this;
    }

    public File save() {
        return null; // TODO
    }

    public File pickFile() {
        return null; // TODO
    }

    public File pickFiles() {
        return null; // TODO
    }

    public File pickFolder() {
        return null; // TODO
    }

//    /**
//     * Selects a single file or folder, depending on the {@link #mode(PickerMode)}
//     * you set.
//     * 
//     * @return null, if the user cancelled the picker operation.
//     */
//    public @Nullable File pickSingle() {
//        SaucerPointerReference<String> pathRef = null;
//        try {
//            if (this.mode == PickerMode.FILES_ONLY) {
//                pathRef = _desktop.N.saucer_desktop_pick_file(SaucerApp.ntv_desktop(), $ref);
//            } else {
//                pathRef = _desktop.N.saucer_desktop_pick_folder(SaucerApp.ntv_desktop(), $ref);
//            }
//
//            if (pathRef == null || pathRef.isNull()) {
//                return null;
//            }
//
//            String pathStr = pathRef.asString();
//            return new File(pathStr);
//        } finally {
//            if (pathRef != null) {
//                pathRef.free();
//            }
//        }
//    }

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

}
