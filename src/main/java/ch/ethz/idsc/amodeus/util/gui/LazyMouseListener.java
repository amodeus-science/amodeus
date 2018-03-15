/* The code was developed by Jan Hakenberg in 2010-2014.
 * The file was released to public domain by the author. */

package ch.ethz.idsc.amodeus.util.gui;

import java.awt.event.MouseEvent;

public interface LazyMouseListener {
    void lazyClicked(MouseEvent mouseEvent);

    default void lazyDragged(MouseEvent mouseEvent) {
        // empty by default
    }
}
