package app.saucer.bundler.util;

import java.io.File;

import app.saucer.bundler.Bundler;
import app.saucer.bundler.BundlerAbortError;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;

public class FileUtil {

    public static void deleteRecursively(File file) {
        if (!file.exists()) return;
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                deleteRecursively(sub);
            }
        }
        file.delete();
    }

    public static void setExecutable(File file) {
        if (Platform.osDistribution == OSDistribution.WINDOWS_NT) {
            Bundler.LOGGER.warn(
                "Windows doesn't support marking files (%s) as executable, this will likely cause problems for your users. I hope you know what you are doing.",
                file.getAbsolutePath()
            );
            return;
        }

        if (!file.setExecutable(true, false)) {
            Bundler.LOGGER.fatal("Unable to mark %s as executable, aborting.", file);
            throw new BundlerAbortError(Bundler.EXIT_CODE_ERROR);
        }
    }

}
