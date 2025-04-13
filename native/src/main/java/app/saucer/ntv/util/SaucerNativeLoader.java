package app.saucer.ntv.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import com.sun.jna.Library;
import com.sun.jna.Native;

import app.saucer.ntv.backend.SaucerBackend;
import app.saucer.ntv.backend.SaucerBackend.FindThisSaucerBackend;
import lombok.Getter;
import lombok.NonNull;

public class SaucerNativeLoader {
    private static DummyLibrary mainSaucerLib = null;

    private static @Getter SaucerBackend backend;

    private static Map<String, String> ep = new HashMap<>();

    private static final String PROPERTY_DO_NOT_EXTRACT = "saucer.java.natives.donotextract";
    private static final String PROPERTY_FORCE_BACKEND = "saucer.java.natives.forcebackend";

    static {
        System.getProperties()
            .entrySet()
            .forEach(
                (e) -> ep.put(
                    ((String) e.getKey())
                        .toLowerCase(), // "saUceR.java.ExamPle" -> "saucer.java.example"
                    (String) e.getValue()
                )
            );
        System.getenv() // Make the environment variables take priority.
            .entrySet()
            .forEach(
                (e) -> ep.put(
                    e.getKey()
                        .replace('_', '.') // "SAUCER_JAVA_EXAMPLE" -> "SAUCER.JAVA.EXAMPLE"
                        .toLowerCase(), // "SAUCER.JAVA.EXAMPLE" -> "saucer.java.example"
                    e.getValue()
                )
            );
    }

    public static <T extends Library> T load(@NonNull Class<T> clazz) {
        return load(clazz, null);
    }

    public static <T extends Library> T load(@NonNull Class<T> clazz, @Nullable String module) {
        if (mainSaucerLib == null) {
            loadNatives();
            mainSaucerLib = Native.load("saucer", DummyLibrary.class);
        }

        String libName = module == null ? "saucer-bindings" : "saucer-bindings-" + module;
        return Native.load(
            libName,
            clazz
        );
    }

    private static void loadNatives() {
        SaucerNativeLoader.class.getClassLoader().setPackageAssertionStatus("app.saucer", true);

        try {
            @SuppressWarnings("deprecation")
            List<SaucerBackend> backends = new Reflections(SaucerResourceUtil.class.getPackageName())
                .getTypesAnnotatedWith(FindThisSaucerBackend.class)
                .stream()
                .map((clazz) -> {
                    try {
                        return (SaucerBackend) clazz.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter((instance) -> instance != null)
                .sorted(Collections.reverseOrder((b1, b2) -> { // Descending.
                    FindThisSaucerBackend b1a = b1.getClass().getDeclaredAnnotation(FindThisSaucerBackend.class);
                    FindThisSaucerBackend b2a = b2.getClass().getDeclaredAnnotation(FindThisSaucerBackend.class);
                    return Integer.compare(b1a.value(), b2a.value());
                }))
                .collect(Collectors.toList());

            SaucerBackend chosenBackend = null;
            if (ep.containsKey(PROPERTY_FORCE_BACKEND)) {
                for (SaucerBackend backend : backends) {
                    if (backend.canLoad()) {
                        chosenBackend = backend;
                        break;
                    }
                }
            } else {
                // We don't even check if it's compatible. We trust that the user knows what
                // they're doing in this case.
                String forced = ep.get(PROPERTY_FORCE_BACKEND);
                for (SaucerBackend backend : backends) {
                    if (backend.getType().name().equalsIgnoreCase(forced) || backend.getType().toString().equalsIgnoreCase(forced)) {
                        chosenBackend = backend;
                        break;
                    }
                }
            }

            if (ep.getOrDefault(PROPERTY_DO_NOT_EXTRACT, "false").equals("false")) {
                if (chosenBackend == null) {
                    throw new RuntimeException("Unable to load a suitable backend");
                }

                backend = chosenBackend;

                Path targetDir = Files.createTempDirectory("saucer-" + System.currentTimeMillis()).normalize();
                chosenBackend.extractTo(targetDir);

                String currentPath = System.getProperty("jna.library.path");
                if (currentPath == null) {
                    System.setProperty("jna.library.path", targetDir.toString());
                } else {
                    System.setProperty("jna.library.path", currentPath + File.pathSeparator + targetDir.toString());
                }
            }
        } catch (RuntimeException bounce) {
            throw bounce;
        } catch (Throwable t) {
            throw new RuntimeException("An error occurred whilst loading Saucer natives", t);
        }
    }

}
