/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Color;

/** the {@link InfoString} is displayed in viewer */
/* package */ class InfoString {
    public final String message;
    public Color color = Color.BLACK;

    public InfoString(String message) {
        this.message = message;
    }

}
