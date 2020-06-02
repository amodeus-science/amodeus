/* The code was developed by Jan Hakenberg in 2010-2014.
 * The file was released to public domain by the author. */

package amodeus.amodeus.util.gui;

/** callback function for use with SpinnerLabel */
@FunctionalInterface
public interface SpinnerListener<Type> {
    /** @param value of selected entry in spinner label */
    void actionPerformed(Type value);
}
