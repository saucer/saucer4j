package app.saucer.utils;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
@JsonClass(exposeAll = true, unsafeInstantiation = true)
public class SaucerSize {
    // Boxed type required for Json serialization. That's the price we pay (:
    public final Integer width;
    public final Integer height;

}
