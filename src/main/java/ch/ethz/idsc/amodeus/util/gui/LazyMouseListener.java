/* The code was developed by Jan Hakenberg in 2010-2014.
 * The file was released to public domain by the author. */

package ch.ethz.idsc.amodeus.util.gui;

import java.awt.event.MouseEvent;

/** interface with callback functions for use with LayzMouse */
public interface LazyMouseListener {
    /** @param mouseEvent where the pressed and clicked happened sufficiently close together */
    void lazyClicked(MouseEvent mouseEvent);

    /** @param mouseEvent when mouse is dragged and does not qualify for a lazy click anymore */
    default void lazyDragged(MouseEvent mouseEvent) {
        // empty by default
    }
}
