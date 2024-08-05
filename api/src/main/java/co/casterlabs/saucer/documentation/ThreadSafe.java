package co.casterlabs.saucer.documentation;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;

/**
 * Anything annotated with @ThreadSafe means that it can be accessed from any
 * thread, not just the main run thread.
 */
@Retention(SOURCE)
public @interface ThreadSafe {

}
