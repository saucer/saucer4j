package co.casterlabs.saucer._impl;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.sun.jna.Library;
import com.sun.jna.Native;

import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.commons.platform.LinuxLibC;
import co.casterlabs.commons.platform.Platform;
import lombok.SneakyThrows;

class Resources {
    private static final String HELP_URL_BASE = "https://example.com";

    private static final String HELP_URL_SUPPORTED_PLATFORMS = HELP_URL_BASE + "/supported-platforms";
    private static final String HELP_URL_REQUIRED_DEPENDENCIES = HELP_URL_BASE + "/required-dependencies?system=%s&arch=%s&isGnu=%b";

    private static final Map<SystemTarget, Map<String /*Arch target*/, List<Backend>>> SUPPORT_MAP = Map.of(
        SystemTarget.GNU_LINUX, Map.of(
            "aarch64", Arrays.asList(Backend.QT5, Backend.QT6),
            "mips64el", Arrays.asList(Backend.QT5),
            "x86_64", Arrays.asList(Backend.QT5, Backend.QT6)
        ),
        SystemTarget.WINDOWS_NT, Map.of(
            "x86_64", Arrays.asList(Backend.WEBVIEW2)
        )
    );

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
        Resources.class.getClassLoader().setPackageAssertionStatus("co.casterlas.saucer", true);

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {}

        try {
            SystemTarget system = SystemTarget.getCurrent();
            String archTarget = Platform.archTarget;

            List<Backend> supportedBackends = SUPPORT_MAP
                .getOrDefault(system, Collections.emptyMap())
                .getOrDefault(archTarget, Collections.emptyList());

            if (supportedBackends.isEmpty()) {
                showError(
                    String.format(
                        "Unsupported platform: %s/%s",
                        system, archTarget
                    ),
                    "More info",
                    HELP_URL_SUPPORTED_PLATFORMS
                ); // Throws.
            }

            Backend chosenBackend = null;
            if (EnvironmentAndProperties.natives_forceBackend == null) {
                for (Backend backend : supportedBackends) {
                    if (backend.canLoad()) {
                        chosenBackend = backend;
                        break;
                    }
                }
            } else {
                // We don't even check if it's compatible. We trust that the user knows what
                // they're doing in this case.
                chosenBackend = Backend.valueOf(EnvironmentAndProperties.natives_forceBackend.toUpperCase());
            }

            if (chosenBackend == null) {
                boolean isGnu = false;
                try {
                    isGnu = LinuxLibC.isGNU();
                } catch (Throwable ignored) {}

                showError(
                    "Couldn't load a suitable backend.",
                    "Install dependencies",
                    String.format(HELP_URL_REQUIRED_DEPENDENCIES, system, archTarget, isGnu)
                ); // Throws.
            }

            if (!EnvironmentAndProperties.natives_doNotExtract) {
                String resourcePath = String.format("natives/%s/%s/%s.zip", chosenBackend.name(), system.name(), archTarget);
                Path targetDir = Files.createTempDirectory("saucer-" + System.currentTimeMillis()).normalize();

                // Grab the zip file from this Jar, extract it to the temp folder above.
                try (InputStream in = loadResource(resourcePath); ZipInputStream zin = new ZipInputStream(in)) {
                    for (ZipEntry ze; (ze = zin.getNextEntry()) != null;) {
                        Path resolvedPath = targetDir.resolve(ze.getName()).normalize();
                        if (!resolvedPath.startsWith(targetDir)) {
                            // https://snyk.io/research/zip-slip-vulnerability
                            throw new RuntimeException("Attempted zip-slip!" + ze.getName());
                        }
                        if (ze.isDirectory()) {
                            Files.createDirectories(resolvedPath);
                        } else {
                            Files.createDirectories(resolvedPath.getParent());
                            Files.copy(zin, resolvedPath);
                        }
                    }
                }

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

    private static String exec(String... command) throws IOException {
        return StreamUtil.toString(
            Runtime.getRuntime().exec(command).getInputStream(),
            Charset.defaultCharset()
        );
    }

    private static boolean checkForLibraries(String... libs) {
        try {
            for (String lib : libs) {
                Native.load(lib, Dummy.class);
            }
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
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
                try {
                    Desktop
                        .getDesktop()
                        .browse(URI.create(helpUrl));
                } catch (IOException | UnsupportedOperationException ignored) {
                    // The yucky.
                    switch (Platform.osDistribution) {
                        case MACOS:
                            Runtime.getRuntime().exec(new String[] {
                                    "open",
                                    helpUrl
                            });
                            break;

                        case WINDOWS_NT:
                            Runtime.getRuntime().exec(new String[] {
                                    "rundll32",
                                    "url.dll,FileProtocolHandler",
                                    helpUrl
                            });
                            break;

                        case LINUX:
                            Runtime.getRuntime().exec(new String[] {
                                    "xdg-open",
                                    helpUrl
                            });
                            break;

                        default:
                            break;
                    }
                }
            }
        }
        throw new RuntimeException(message + " | " + buttonName + ": " + helpUrl);
    }

    static interface Dummy extends Library {

    }

    // In order of preference.
    private static enum Backend {
        QT6 {
            @Override
            public boolean canLoad() throws IOException {
                return checkForLibraries(
                    "Qt6WebEngineCore",
                    "Qt6WebEngineWidgets",
                    "Qt6WebChannel",
                    "Qt6Widgets",
                    "Qt6Core"
                );
            }
        },

        QT5 {
            @Override
            public boolean canLoad() throws IOException {
                return checkForLibraries(
                    "Qt5WebEngineCore",
                    "Qt5WebEngineWidgets",
                    "Qt5WebChannel",
                    "Qt5Widgets",
                    "Qt5Core"
                );
            }
        },

        WEBVIEW2 {
            @Override
            public boolean canLoad() throws IOException {
                if (Platform.wordSize == 32) return false; // We don't support 32 bit (yet). s00n.

                // https://learn.microsoft.com/en-us/microsoft-edge/webview2/concepts/distribution?tabs=dotnetcsharp#detect-if-a-webview2-runtime-is-already-installed

                if (!exec("reg", "query", "HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\Microsoft\\EdgeUpdate\\Clients\\{F3017226-FE2A-4295-8BDF-00C3A9A7E4C5}", "/v", "pv").contains("ERROR: ")) return true;

                if (!exec("reg", "query", "HKEY_CURRENT_USER\\Software\\Microsoft\\EdgeUpdate\\Clients\\{F3017226-FE2A-4295-8BDF-00C3A9A7E4C5}", "/v", "pv").contains("ERROR: ")) return true;

                return false;
            }
        },
        ;

        public abstract boolean canLoad() throws IOException;

    }

    private static enum SystemTarget {
        GNU_LINUX,
        WINDOWS_NT,;

        public static SystemTarget getCurrent() throws IOException {
            switch (Platform.osDistribution) {
                case LINUX:
                    if (LinuxLibC.isGNU()) {
                        return GNU_LINUX;
                    } else {
                        return null;
                    }

                case WINDOWS_NT:
                    return WINDOWS_NT;

                default:
                    return null;
            }
        }
    }

}
