package app.saucer.ntv.documentation;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.util.function.Supplier;

/**
 * Anything annotated with NotThreadSafe means that has to be accessed from the
 * main thread. Either at your application's startup or via
 * {@link SaucerApp#dispatch(Runnable)} or {@link SaucerApp#dispatch(Supplier)}.
 */
@Retention(SOURCE)
public @interface NotThreadSafe {

}
