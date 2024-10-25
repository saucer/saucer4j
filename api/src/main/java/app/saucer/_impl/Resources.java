package app.saucer._impl;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.reflections.Reflections;

import app.saucer._impl._SaucerBackend.FindThisSaucerBackend;
import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.commons.platform.Platform;
import lombok.SneakyThrows;

@SuppressWarnings("deprecation")
class Resources {
    private static boolean alreadyLoaded = false;

    @SneakyThrows
    static String loadResourceString(String name) {
        return StreamUtil.toString(loadResource(name), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    static InputStream loadResource(String name) {
        String fullPath = "app/saucer/_impl/" + name;

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
            List<_SaucerBackend> backends = new Reflections(Resources.class.getPackageName())
                .getTypesAnnotatedWith(FindThisSaucerBackend.class)
                .stream()
                .map((clazz) -> {
                    try {
                        return (_SaucerBackend) clazz.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter((instance) -> instance != null)
                .sorted(Collections.reverseOrder((b1, b2) -> {  // Descending.
                    FindThisSaucerBackend b1a = b1.getClass().getDeclaredAnnotation(FindThisSaucerBackend.class);
                    FindThisSaucerBackend b2a = b2.getClass().getDeclaredAnnotation(FindThisSaucerBackend.class);
                    return Integer.compare(b1a.value(), b2a.value());
                }))
                .collect(Collectors.toList());

            _SaucerBackend chosenBackend = null;
            if (EnvironmentAndProperties.natives_forceBackend == null) {
                for (_SaucerBackend backend : backends) {
                    if (backend.checkDependencies()) {
                        chosenBackend = backend;
                        break;
                    }
                }
            } else {
                // We don't even check if it's compatible. We trust that the user knows what
                // they're doing in this case.
                String forced = EnvironmentAndProperties.natives_forceBackend;
                for (_SaucerBackend backend : backends) {
                    if (backend.getType().equalsIgnoreCase(forced)) {
                        chosenBackend = backend;
                        break;
                    }
                }
            }

            if (chosenBackend == null) {
                showError(
                    "Couldn't load a suitable backend.",
                    "Install dependencies",
                    EnvironmentAndProperties.helpurl_dependencies
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
                // We can't use SaucerDesktop at this point. So manual mode it is :D
                switch (Platform.osDistribution) {
                    case MACOS:
                        // We can't use Desktop/AWT while Saucer is running.
                        Runtime.getRuntime().exec(new String[] {
                                "open",
                                helpUrl
                        });
                        break;

                    case WINDOWS_NT:
                        try {
                            Desktop
                                .getDesktop()
                                .browse(URI.create(helpUrl));
                        } catch (UnsupportedOperationException ignored) {
                            Runtime.getRuntime().exec(new String[] { // Antivirus software may trip the rundll32.
                                    "rundll32",
                                    "url.dll,FileProtocolHandler",
                                    helpUrl
                            });
                        }
                        break;

                    case LINUX:
                        try {
                            Desktop
                                .getDesktop()
                                .browse(URI.create(helpUrl));
                        } catch (UnsupportedOperationException ignored) {
                            Runtime.getRuntime().exec(new String[] { // The user might not have xdg-open.
                                    "xdg-open",
                                    helpUrl
                            });
                        }
                        break;

                    default:
                        break;
                }
            }
        }
        throw new RuntimeException(message + " | " + buttonName + ": " + helpUrl);
    }

}
