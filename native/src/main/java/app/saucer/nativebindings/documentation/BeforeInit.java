package app.saucer.nativebindings.documentation;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;

/**
 * This annotation is used to mark a method as being called before the
 * initialization of Saucer.
 */
@Retention(SOURCE)
public @interface BeforeInit {

}
