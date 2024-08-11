package co.casterlabs.saucer.documentation;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;

import co.casterlabs.saucer.SaucerWindow;

/**
 * Anything annotated with NotThreadSafe means that has to be accessed from the
 * main thread. Either at your application's startup or via
 * {@link SaucerWindow#dispatch(Runnable)}
 */
@Retention(SOURCE)
public @interface NotThreadSafe {

}
