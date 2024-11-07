package app.saucer.bundler.cli;

import java.io.File;
import java.util.Collections;
import java.util.List;

import app.saucer.bundler.Bundler;
import app.saucer.bundler.BundlerAbortError;
import app.saucer.bundler.config.BuildExecutableSubsystem;
import app.saucer.bundler.config.BuildTargetArch;
import app.saucer.bundler.config.BuildTargetOS;
import lombok.Getter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Getter
@Command(name = "bundle", mixinStandardHelpOptions = true, description = "Bundles your app using the provided parameters.")
public class CommandBundle implements Runnable {
    @Option(names = {
            "-o",
            "--os"
    }, description = "The target operating system to bundle for.", required = true)
    private BuildTargetOS targetOS;

    @Option(names = {
            "-a",
            "--arch"
    }, description = "The target CPU architecture to bundle for.", required = true)
    private BuildTargetArch targetArch;

    @Option(names = {
            "-j",
            "--java"
    }, description = "The Java version to download.", required = true)
    private int targetJava;

    @Option(names = {
            "-s",
            "--subsystem"
    }, description = "The executable subsystem to use. This option is ignored when used outside of macOS and Windows builds. Default: window\n\n"
        + "On Windows, this changes which subsystem is used. Refer to the Microsoft documentation to see what this does: https://tinyurl.com/4z39mbyr\n\n"
        + "When 'window' is used on macOS, this generates an app bundle.")
    private BuildExecutableSubsystem subsystem = BuildExecutableSubsystem.window;

    @Option(names = {
            "-d",
            "--dependency"
    }, description = "A list of dependencies to add to the classpath. Use this format:\n"
        + "'https://repo|group:artifact:version:suffix'\n or 'file.suffix'"
        + "e.g\n"
        + "'mydependency.jar'\n"
        + "or\n"
        + "'https://repo.maven.apache.org/maven2|com.example:example:1.0.0:.jar'\n"
        + "Note the suffix on Maven dependencies. That can be used to target different types, such as .war. You can also use it to download shaded artifacts by using '-shaded.jar'.")
    private List<String> dependencies = Collections.emptyList();

    @Option(names = {
            "-r",
            "--arg"
    }, description = "Additional VM arguments.")
    private List<String> additionalVmArgs = Collections.emptyList();

    @Option(names = {
            "-f",
            "--file"
    }, description = "A list of additional files to add to the resulting bundle.")
    private List<File> additionalFiles = Collections.emptyList();

    @Option(names = {
            "-n",
            "--name"
    }, description = "The final name of the generated bundle. Some platforms have restrictions on what characters can be used, it is best to stick to standard ASCII.", required = true)
    private String name;

    @Option(names = {
            "-i",
            "--id"
    }, description = "The ID of your app. This does nothing on Windows.", required = true)
    private String id;

    @Option(names = {
            "-c",
            "--icon"
    }, description = "The icon of your app.")
    private File icon;

    @Option(names = {
            "-m",
            "--main"
    }, description = "The main class to invoke.", required = true)
    private String main;

    @Override
    public void run() {
        try {
            new Bundler(this).build();
        } catch (BundlerAbortError e) {
            System.exit(e.desiredExitCode);
        } catch (Throwable t) {
            Bundler.LOGGER.severe("An error occurred whilst bundling! Aborting...\n%s", t);
            System.exit(Bundler.EXIT_CODE_ERROR);
        }
    }

}
