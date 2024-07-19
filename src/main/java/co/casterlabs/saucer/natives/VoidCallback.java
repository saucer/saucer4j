package co.casterlabs.saucer.natives;

import com.sun.jna.Callback;

/**
 * @implNote Do not inline this. The JVM needs this to always be accessible
 *           otherwise it will garbage collect and ruin our day.
 */
interface VoidCallback extends Callback {

    void callback();

}
