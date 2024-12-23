package app.saucer.bundler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import app.saucer.bundler.cli.CommandBundle;
import app.saucer.bundler.config.BuildExecutableSubsystem;
import app.saucer.bundler.config.BuildTargetOS;
import app.saucer.bundler.util.Adoptium;
import app.saucer.bundler.util.FileUtil;
import app.saucer.bundler.util.Icon;
import app.saucer.bundler.util.LauncherUtil;
import app.saucer.bundler.util.Maven;
import app.saucer.bundler.util.PEUtil;
import app.saucer.bundler.util.archive.ArchiveCreator;
import app.saucer.bundler.util.archive.ArchiveExtractor;
import app.saucer.bundler.util.archive.Archives;
import app.saucer.bundler.util.archive.Archives.Format;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class Bundler {
    public static final int EXIT_CODE_ERROR = 255;
    public static final int EXIT_CODE_OTHER = 2;
    public static final int EXIT_CODE_SUCCESS = 0;

    public static final File BASE_FOLDER = new File("./dist/");
    public static final File BUILD_FOLDER = new File(BASE_FOLDER, "build");
    public static final File ARTIFACTS_FOLDER = new File(BASE_FOLDER, "artifacts");
    public static final File DOWNLOAD_CACHE_FOLDER = new File(BASE_FOLDER, "download-cache");

    static {
        BASE_FOLDER.mkdirs();
        BUILD_FOLDER.mkdirs();
        ARTIFACTS_FOLDER.mkdirs();
        DOWNLOAD_CACHE_FOLDER.mkdirs();
    }

    public static final FastLogger LOGGER = new FastLogger("Bundler");

    private final CommandBundle buildOptions;
    private final File buildFolder;

    private List<String> classpath = new LinkedList<>();

    public Bundler(CommandBundle buildOptions) {
        this.buildOptions = buildOptions;

        File baseFolder = new File(
            BUILD_FOLDER, String.format(
                "%s-%s",
                this.buildOptions.getTargetOS(), this.buildOptions.getTargetArch()
            )
        );

        boolean makeMacOSBundle = this.buildOptions.getTargetOS() == BuildTargetOS.macos && this.buildOptions.getSubsystem() == BuildExecutableSubsystem.window;
        if (makeMacOSBundle) {
            this.buildFolder = new File(baseFolder, this.buildOptions.getName() + ".app");
        } else {
            this.buildFolder = baseFolder;
        }

        FileUtil.deleteRecursively(this.buildFolder); // Clear it up.
        this.buildFolder.mkdirs();
    }

    public void build() {
        this.downloadRuntime();
        this.processIncludes();
        this.create();
    }

    private void downloadRuntime() {
        boolean makeMacOSBundle = this.buildOptions.getTargetOS() == BuildTargetOS.macos && this.buildOptions.getSubsystem() == BuildExecutableSubsystem.window;

        Bundler.LOGGER.info("runtime -> Downloading...");
        File runtimeFolder = makeMacOSBundle ? this.buildFolder : new File(this.buildFolder, "runtime");

        File archive;
        try {
            archive = Adoptium.download(this.buildOptions.getTargetJava(), this.buildOptions.getTargetArch(), this.buildOptions.getTargetOS());
        } catch (IllegalArgumentException e) {
            Bundler.LOGGER.warn("runtime -> Unsupported build target, aborting.\n%s", e);
            throw new BundlerAbortError(Bundler.EXIT_CODE_OTHER);
        } catch (IOException | InterruptedException e) {
            Bundler.LOGGER.fatal("runtime -> Unable to download, aborting.\n%s", e);
            throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
        }

        Bundler.LOGGER.info("runtime -> Extracting...");
        try {
            ArchiveExtractor.extract(
                Archives.probeFormat(archive),
                archive,
                runtimeFolder
            );
        } catch (IOException e) {
            Bundler.LOGGER.fatal("runtime -> Unable to extract, aborting.\n%s", e);
            throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
        }

        Bundler.LOGGER.info("runtime -> Reorganizing...");
        // Clean it all up.
        try {
            if (runtimeFolder.list().length == 1) {
                // It's nested. Let's fix that.
                File nestedFolder = runtimeFolder.listFiles()[0];
                Bundler.LOGGER.debug("runtime -> Reorganizing files.");
                for (File nestedFolderChild : nestedFolder.listFiles()) {
                    Files.move(nestedFolderChild.toPath(), new File(runtimeFolder, nestedFolderChild.getName()).toPath());
                }
                nestedFolder.delete();
            }

            if (makeMacOSBundle) {
                // Extract the bundle.
                new File(runtimeFolder, "Contents/MacOS").mkdirs();
                new File(runtimeFolder, "Contents/Resources").mkdirs();
                Files.move(new File(runtimeFolder, "Contents/Home").toPath(), new File(runtimeFolder, "Contents/Resources/runtime").toPath());

                FileUtil.deleteRecursively(new File(runtimeFolder, "Contents/Resources/runtime/man")); // Delete any manpages. (macOS)
                FileUtil.deleteRecursively(new File(runtimeFolder, "Contents/Resources/runtime/docs")); // Delete any manpages. (macOS)
                FileUtil.deleteRecursively(new File(runtimeFolder, "Contents/_CodeSignature")); // Delete any code signatures (macOS)
                FileUtil.deleteRecursively(new File(runtimeFolder, "Contents/Info.plist")); // Delete any manifests (macOS)
            } else if (this.buildOptions.getTargetOS() == BuildTargetOS.macos) {
                // TODO macOS CLI apps. We'll need to extract the VM properly.
            } else {
                FileUtil.deleteRecursively(new File(runtimeFolder, "man")); // Delete any manpages
                FileUtil.deleteRecursively(new File(runtimeFolder, "docs")); // Delete any manpages
            }
        } catch (IOException e) {
            Bundler.LOGGER.fatal("runtime -> Unable to reorganize files, aborting.\n%s", e);
            throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
        }
    }

    private void processIncludes() {
        boolean makeMacOSBundle = this.buildOptions.getTargetOS() == BuildTargetOS.macos && this.buildOptions.getSubsystem() == BuildExecutableSubsystem.window;
        File includesFolder = makeMacOSBundle ? new File(this.buildFolder, "Contents/Resources") : this.buildFolder;

        Bundler.LOGGER.info("includes -> Gathering dependencies...");
        for (String dependency : this.buildOptions.getDependencies()) {
            if (!dependency.contains("|")) { // Normal file.
                try {
                    Bundler.LOGGER.info("includes -> Adding %s...", dependency);
                    File dependencyFile = new File(dependency);
                    File target = new File(includesFolder, dependencyFile.getName());
                    Files.copy(dependencyFile.toPath(), target.toPath());

                    this.classpath.add(target.getName());
                } catch (IOException e) {
                    Bundler.LOGGER.fatal("includes -> Unable to copy dependency, aborting.\n%s", e);
                    throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
                }
                continue;
            }

            Bundler.LOGGER.info("includes -> Downloading %s...", dependency);

            // Format: repo|group:artifact:version:suffix
            String repo;
            String groupId;
            String artifactId;
            String version;
            String suffix;
            {
                String[] splitDep = dependency.split("\\|", 2);
                repo = splitDep[0];

                String[] depParts = splitDep[1].split(":", 4);
                groupId = depParts[0];
                artifactId = depParts[1];
                version = depParts[2];
                suffix = depParts[3];
            }

            try {
                File downloaded = Maven.downloadDependency(repo, groupId, artifactId, version, suffix);
                File target = new File(includesFolder, downloaded.getName());
                Files.copy(downloaded.toPath(), target.toPath());
                this.classpath.add(target.getName());
            } catch (IOException | InterruptedException e) {
                Bundler.LOGGER.fatal("includes -> Unable to copy dependency, aborting.\n%s", e);
                throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
            }
        }

        Bundler.LOGGER.info("includes -> Gathering files...");
        for (File file : this.buildOptions.getAdditionalFiles()) {
            try {
                Bundler.LOGGER.info("includes -> Adding %s...", file);
                File target = new File(includesFolder, file.getName());
                Files.copy(file.toPath(), target.toPath());
            } catch (IOException e) {
                Bundler.LOGGER.fatal("includes -> Unable to copy dependency, aborting.\n%s", e);
                throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
            }
        }
    }

    private void create() {
        boolean makeMacOSBundle = this.buildOptions.getTargetOS() == BuildTargetOS.macos && this.buildOptions.getSubsystem() == BuildExecutableSubsystem.window;
        File resourcesFolder = makeMacOSBundle ? new File(this.buildFolder, "Contents/Resources") : this.buildFolder;

        try {
            Bundler.LOGGER.info("create -> Building arguments...");
            JsonArray arguments = new JsonArray();

            arguments.add(
                String.format(
                    "-Djava.class.path=%s",
                    String.join(
                        this.buildOptions.getTargetOS() == BuildTargetOS.windows ? ";" : ":",
                        this.classpath
                    )
                )
            );
            this.buildOptions.getAdditionalVmArgs().forEach(arguments::add);

            String main = this.buildOptions.getMain();

            String bundleInfo = new JsonObject()
                .put("args", arguments)
                .put("main", main)
                .toString(false);

            Files.writeString(new File(resourcesFolder, "bundle.json").toPath(), bundleInfo);
        } catch (IOException e) {
            Bundler.LOGGER.fatal("create -> Unable to write vmargs.txt, aborting.\n%s", e);
            throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
        }

        Bundler.LOGGER.info("create -> Creating executable...");
        try {
            if (makeMacOSBundle) {
                try (InputStream in = LauncherUtil.get(this.buildOptions.getTargetArch(), this.buildOptions.getTargetOS());
                    OutputStream out = new FileOutputStream(new File(this.buildFolder, "Contents/MacOS/" + this.buildOptions.getName()))) {
                    in.transferTo(out);
                }
                try {
                    Files.writeString(
                        new File(this.buildFolder, "Contents/Info.plist").toPath(),
                        LauncherUtil.MACOS_BUNDLE_PLIST_TEMPLATE
                            .replace("{name}", this.buildOptions.getName())
                            .replace("{id}", this.buildOptions.getId())
                    );
                } catch (IOException e) {
                    LOGGER.fatal("create -> Unable to write Info.plist, aborting.\n%s", e);
                    throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
                }
            } else {
                String executableName = this.buildOptions.getName() + LauncherUtil.fileExtension(this.buildOptions.getTargetOS());

                try (InputStream in = LauncherUtil.get(this.buildOptions.getTargetArch(), this.buildOptions.getTargetOS());
                    OutputStream out = new FileOutputStream(new File(resourcesFolder, executableName))) {
                    if (in == null) {
                        LOGGER.fatal("create -> Unable to find native executable suitable for target OS & Arch, aborting.");
                        throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
                    }

                    in.transferTo(out);
                }
            }
        } catch (IOException e) {
            LOGGER.fatal("create -> Unable to copy native executable, aborting.\n%s", e);
            throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
        }

        switch (this.buildOptions.getTargetOS()) {
            case macos: {
                Bundler.LOGGER.info("create -> Marking files as executable...");
                final String[] NEED_TO_MARK_EXEC = {
                        this.buildOptions.getName(),  // Non-bundle mode
                        "Contents/MacOS/" + this.buildOptions.getName(), // Bundle mode
                        "../"  // Bundle mode
                };
                for (String path : NEED_TO_MARK_EXEC) {
                    File file = new File(this.buildFolder, path);
                    if (file.exists()) {
                        FileUtil.setExecutable(file);
                    }
                }
                break;
            }

            case windows:
                // NOOP
                break;

            default: {
                Bundler.LOGGER.info("create -> Marking files as executable...");
                final String[] NEED_TO_MARK_EXEC = {
                        this.buildOptions.getName()
                };
                for (String path : NEED_TO_MARK_EXEC) {
                    File file = new File(this.buildFolder, path);
                    FileUtil.setExecutable(file);
                }
                break;
            }

        }

        if (this.buildOptions.getIcon() != null) {
            Bundler.LOGGER.info("create -> Creating icon...");
            try {
                Icon icon = Icon.from(this.buildOptions.getIcon());
                switch (this.buildOptions.getTargetOS()) {
                    case macos:
                        Files.write(
                            new File(resourcesFolder, "icon.icns").toPath(),
                            icon.toIcns()
                        );
                        break;

//                    case aix:
                    case gnulinux:
//                    case musllinux:
//                    case solaris:
                        Files.write(
                            new File(resourcesFolder, "icon.png").toPath(),
                            icon.toPng()
                        );
                        break;

                    case windows:
                        Files.write(
                            new File(resourcesFolder, "icon.ico").toPath(),
                            icon.toIco()
                        );
                        PEUtil.setExeIcon(new File(this.buildFolder, this.buildOptions.getName() + ".exe"), new File(resourcesFolder, "icon.ico"));
                        break;
                }
            } catch (IOException | InterruptedException e) {
                LOGGER.fatal("create -> Unable to write image icon, aborting.\n%s", e);
                throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
            }
        }

        if (this.buildOptions.getSignCommand() != null) {
            LOGGER.info("create -> Signing executable... (%s)", this.buildOptions.getSignCommand());
            try {
                FastLogger commandLogger = new FastLogger("Sign command");
                File workingDir = this.buildOptions.getTargetOS() == BuildTargetOS.macos ? this.buildFolder.getParentFile() : this.buildFolder;
                Process proc = Runtime.getRuntime().exec(this.buildOptions.getSignCommand(), null, workingDir);

                Thread stdoutThread = new Thread(() -> {
                    try (Scanner sc = new Scanner(proc.getInputStream())) {
                        while (sc.hasNextLine()) {
                            commandLogger.info(sc.nextLine());
                        }
                    } catch (Exception ignored) {}
                });
                Thread stderrThread = new Thread(() -> {
                    try (Scanner sc = new Scanner(proc.getErrorStream())) {
                        while (sc.hasNextLine()) {
                            commandLogger.severe(sc.nextLine());
                        }
                    } catch (Exception ignored) {}
                });

                stdoutThread.start();
                stderrThread.start();

                stdoutThread.join();
                stderrThread.join();
                int exitValue = proc.waitFor();
                if (exitValue != 0) {
                    throw new Exception("Process exited with code " + exitValue);
                }
            } catch (Exception e) {
                LOGGER.fatal("create -> Unable to sign executable, aborting.\n%s", e);
                throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
            }
        }

        LOGGER.info("create -> Creating artifact...");
        File archiveFile = null;
        switch (this.buildOptions.getTargetOS()) {
            case macos: {
                try {
                    archiveFile = new File(Bundler.ARTIFACTS_FOLDER, String.format("%s-%s-%s.tar.gz", this.buildOptions.getName(), this.buildOptions.getTargetOS(), this.buildOptions.getTargetArch()));
                    ArchiveCreator.create(Format.TAR_GZ, this.buildFolder.getParentFile(), archiveFile);
                } catch (IOException e) {
                    LOGGER.fatal("create -> Unable to create .tar.gz file, aborting.\n%s", e);
                    throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
                }
                break;
            }

//            case aix:
            case gnulinux:
//            case musllinux:
//            case solaris:
            {
                try {
                    archiveFile = new File(Bundler.ARTIFACTS_FOLDER, String.format("%s-%s-%s.tar.gz", this.buildOptions.getName(), this.buildOptions.getTargetOS(), this.buildOptions.getTargetArch()));
                    ArchiveCreator.create(Format.TAR_GZ, this.buildFolder, archiveFile);
                } catch (IOException e) {
                    LOGGER.fatal("create -> Unable to create .tar.gz file, aborting.\n%s", e);
                    throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
                }
                break;
            }

            case windows:
                try {
                    archiveFile = new File(Bundler.ARTIFACTS_FOLDER, String.format("%s-%s-%s.zip", this.buildOptions.getName(), this.buildOptions.getTargetOS(), this.buildOptions.getTargetArch()));
                    ArchiveCreator.create(Format.ZIP, this.buildFolder, archiveFile);
                } catch (IOException e) {
                    LOGGER.fatal("create -> Unable to create .tar.gz file, aborting.\n%s", e);
                    throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
                }
                break;
        }

        LOGGER.info("create -> Done! Produced artifact: %s", archiveFile.getAbsolutePath());
    }

}
