/* The code was developed by Jan Hakenberg in 2010-2014.
 * The file was released to public domain by the author. */

package amodeus.amodeus.util.net;

/** callback function for use with ObjectSocket, and ObjectClient */
@FunctionalInterface
public interface ObjectHandler {
    /** @param object received via socket */
    void handle(Object object);
}
