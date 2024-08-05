package co.casterlabs.saucer._impl;

import java.util.concurrent.Future;
import java.util.function.Supplier;

import co.casterlabs.saucer.Saucer;
import co.casterlabs.saucer.Saucer.SaucerRunStrategy;
import co.casterlabs.saucer.documentation.InternalUseOnly;
import co.casterlabs.saucer.utils.SaucerOptions;
import lombok.NonNull;

/**
 * This class only exists to expose some methods to the public package.
 */
@Deprecated
@InternalUseOnly
public class _PackageBridge {

    @Deprecated
    public static Saucer create(@NonNull SaucerOptions options) {
        return new ImplSaucer(options);
    }

    @Deprecated
    public static <T> Future<T> dispatch(@NonNull Supplier<T> task) {
        return ImplSaucer.dispatch(task);
    }

    @Deprecated
    public static void run(@NonNull SaucerRunStrategy strategy) {
        ImplSaucer.run(strategy);
    }

    public static <T> T dispatchSync(@NonNull Supplier<T> task) {
        return ImplSaucer.dispatchSync(task);
    }

}
