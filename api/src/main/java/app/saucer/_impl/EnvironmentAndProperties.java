package app.saucer._impl;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

class EnvironmentAndProperties {

    static boolean natives_doNotExtract = false;
    static @Nullable String natives_forceBackend = null;

    static String helpurl_dependencies;

    static {
        Map<String, String> pe = new HashMap<>();
        System.getProperties()
            .entrySet()
            .forEach(
                (e) -> pe.put(
                    ((String) e.getKey())
                        .toLowerCase(), // "saUceR.java.ExamPle" -> "saucer.java.example"
                    (String) e.getValue()
                )
            );
        System.getenv() // Make the environment variables take priority.
            .entrySet()
            .forEach(
                (e) -> pe.put(
                    e.getKey()
                        .replace('_', '.') // "SAUCER_JAVA_EXAMPLE" -> "SAUCER.JAVA.EXAMPLE"
                        .toLowerCase(), // "SAUCER.JAVA.EXAMPLE" -> "saucer.java.example"
                    e.getValue()
                )
            );

        natives_doNotExtract = "true".equalsIgnoreCase(pe.get("saucer.java.natives.donotextract"));
        natives_forceBackend = pe.get("saucer.java.natives.forcebackend");
        helpurl_dependencies = pe.getOrDefault("saucer.java.help.dependencies", "https://saucer.github.io/docs/getting-started/dependencies");
    }

}
