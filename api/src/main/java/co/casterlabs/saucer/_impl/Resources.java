package co.casterlabs.saucer._impl;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.commons.platform.LinuxLibC;
import co.casterlabs.saucer.Saucer;
import lombok.SneakyThrows;

@SuppressWarnings("deprecation")
class Resources {
    private static final String HELP_URL_BASE = "https://example.com";

    private static final String HELP_URL_REQUIRED_DEPENDENCIES = HELP_URL_BASE + "/required-dependencies?system=%s&arch=%s&isGnu=%b";

    private static boolean alreadyLoaded = false;

    @SneakyThrows
    static String loadResourceString(String name) {
        return StreamUtil.toString(loadResource(name), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    static InputStream loadResource(String name) {
        String fullPath = "co/casterlabs/saucer/_impl/" + name;

        InputStream in = Resources.class.getResourceAsStream(fullPath);
        if (in == null) {
            // Some IDEs mangle the resource location when launching directly. Let's try
            // that as a backup.
            in = Resources.class.getResourceAsStream("/" + fullPath);
        }
        if (in == null) {
            // Another mangle.
            in = Resources.class.getResourceAsStream("/resources/" + fullPath);
        }

        assert in != null : "Could not locate internal resource: " + fullPath;

        return in;
    }

    static void loadNatives() {
        if (alreadyLoaded) {
            return;
        }
        alreadyLoaded = true;

        Resources.class.getClassLoader().setPackageAssertionStatus("co.casterlas.saucer", true);

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {}

        try {
            List<Backend> backends = discoverBackends();

            Backend chosenBackend = null;
            if (EnvironmentAndProperties.natives_forceBackend == null) {
                for (Backend backend : backends) {
                    if (backend.canLoad()) {
                        chosenBackend = backend;
                        break;
                    }
                }
            } else {
                // We don't even check if it's compatible. We trust that the user knows what
                // they're doing in this case.
                String forced = EnvironmentAndProperties.natives_forceBackend;
                for (Backend backend : backends) {
                    if (backend.getType().equalsIgnoreCase(forced)) {
                        chosenBackend = backend;
                        break;
                    }
                }
            }

            if (chosenBackend == null) {
                boolean isGnu = false;
                try {
                    isGnu = LinuxLibC.isGNU();
                } catch (Throwable ignored) {}

                showError(
                    "Couldn't load a suitable backend.",
                    "Install dependencies",
                    String.format(HELP_URL_REQUIRED_DEPENDENCIES, Saucer.getSystemTarget(), Saucer.getArchTarget(), isGnu)
                ); // Throws.
            }

            _SaucerNative.backend = chosenBackend.getType();

            if (!EnvironmentAndProperties.natives_doNotExtract) {
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

    @SneakyThrows
    private static void showError(String message, String buttonName, String helpUrl) {
        if (!GraphicsEnvironment.isHeadless()) {
            int chosenResult = JOptionPane.showOptionDialog(
                null,
                message, "Saucer error",
                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                new String[] {
                        buttonName
                }, null
            );

            if (chosenResult == 0) {
                Saucer.openLinkInSystemBrowser(helpUrl);
            }
        }
        throw new RuntimeException(message + " | " + buttonName + ": " + helpUrl);
    }

    private static List<Backend> discoverBackends() {
        List<Backend> discovered = new LinkedList<>();
        String basePackage = Resources.class.getPackageName();

        for (String clazz : Arrays.asList("BackendWebview2")) { // TODO keep in-sync with everything :)
            try {
                discovered.add((Backend) Class.forName(basePackage + '.' + clazz).newInstance());
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ignored) {}
        }

        return discovered;
    }

}
