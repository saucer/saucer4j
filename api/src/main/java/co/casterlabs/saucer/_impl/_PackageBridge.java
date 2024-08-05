package co.casterlabs.saucer._impl;

import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.documentation.InternalUseOnly;
import co.casterlabs.saucer.utils.SaucerOptions;
import lombok.NonNull;

/**
 * This class only exists to expose some methods to the public package.
 */
@SuppressWarnings("deprecation")
@Deprecated
@InternalUseOnly
public class _PackageBridge {

    @Deprecated
    public static Saucer create(@NonNull SaucerOptions options) {
        return new ImplSaucer(options);
    }

    @Deprecated
    public static void run() {
        ImplSaucer.run();
    }

}
