package co.casterlabs.saucer._impl.c;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;

import co.casterlabs.saucer.documentation.InternalUseOnly;

/**
 * This means that our code (Java) should avoid calling free(), as the native
 * code already handles it for us.
 */
@SuppressWarnings("deprecation")
@Deprecated
@InternalUseOnly
@Retention(SOURCE)
public @interface NoFree {

}
