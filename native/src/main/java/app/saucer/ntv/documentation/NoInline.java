package app.saucer.ntv.documentation;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;

/**
 * Anything annotated with NoInline means that you cannot inline this callback
 * as a lambda, as JNA will garbage-collect the callback. You must maintain a
 * reference to the callback for it to work.
 */
@Retention(SOURCE)
public @interface NoInline {

}
