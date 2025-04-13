package app.saucer.ntv.documentation;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates a type is only used for documentation.
 */
@Retention(SOURCE)
@Target({
        ElementType.FIELD,
        ElementType.LOCAL_VARIABLE,
        ElementType.PARAMETER,
        ElementType.TYPE,
        ElementType.TYPE_PARAMETER,
        ElementType.TYPE_USE
})
public @interface DocumentingOnly {

}
