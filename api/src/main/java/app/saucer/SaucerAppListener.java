package app.saucer;

public interface SaucerAppListener {

    /**
     * @return true, if you want to prevent the app from quitting.
     */
    default boolean shouldAvoidQuitting() {
        return false;
    }

    default void onQuit() {}

}
