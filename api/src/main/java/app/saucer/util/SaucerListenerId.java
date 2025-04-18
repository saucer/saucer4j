package app.saucer.util;

import java.util.UUID;

/**
 * This is an opaque type.
 */
public final class SaucerListenerId {
    private int hash = UUID.randomUUID().toString().hashCode();

    @Override
    public int hashCode() {
        return this.hash;
    }
}
