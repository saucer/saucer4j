package app.saucer.util;

import com.sun.jna.ptr.IntByReference;

import app.saucer.ntv.ntv_app;
import app.saucer.ntv.ntv_app.saucer_screen;
import app.saucer.ntv.documentation.InternalUseOnly;
import app.saucer.ntv.util.SaucerBoxedType;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonSerializer;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.ToString;

/**
 * Represents a screen/monitor connected to the system.
 */
@ToString
@JsonClass(serializer = SaucerScreenSerializer.class)
public class SaucerScreen extends SaucerBoxedType<saucer_screen> {

    /**
     * @deprecated Native interop only.
     */
    @Deprecated
    @InternalUseOnly
    public SaucerScreen(@NonNull saucer_screen $ref, boolean autoFree) {
        super($ref, autoFree);
    }

    /**
     * @return The name of the screen.
     */
    @ToString.Include
    public String name() {
        return ntv_app.N.saucer_screen_name($ref);
    }

    /**
     * @return The size of the screen.
     */
    @ToString.Include
    public SaucerSize size() {
        IntByReference widthRef = new IntByReference();
        IntByReference heightRef = new IntByReference();
        ntv_app.N.saucer_screen_size($ref, widthRef, heightRef);
        return new SaucerSize(widthRef.getValue(), heightRef.getValue());
    }

    /**
     * @return The position of the screen.
     */
    @ToString.Include
    public SaucerPosition position() {
        IntByReference xRef = new IntByReference();
        IntByReference yRef = new IntByReference();
        ntv_app.N.saucer_screen_position($ref, xRef, yRef);
        return new SaucerPosition(xRef.getValue(), yRef.getValue());
    }

    /**
     * @return The bounds of the screen.
     */
    public SaucerRectangle bounds() {
        SaucerSize size = this.size();
        SaucerPosition position = this.position();
        return new SaucerRectangle(position.x, position.y, size.width, size.height);
    }

}

class SaucerScreenSerializer implements JsonSerializer<SaucerScreen> {

    @Override
    public JsonElement serialize(@NonNull Object v, @NonNull Rson rson) {
        SaucerScreen value = (SaucerScreen) v;
        return new JsonObject()
            .put("name", value.name())
            .put("size", rson.toJson(value.size()))
            .put("position", rson.toJson(value.position()));
    }

}
