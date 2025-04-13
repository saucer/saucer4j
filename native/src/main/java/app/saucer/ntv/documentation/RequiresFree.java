package app.saucer.ntv.documentation;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import app.saucer.ntv._memory;

/**
 * Requires that {@link _memory#saucer_memory_free()} be called.
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
public @interface RequiresFree {

}
