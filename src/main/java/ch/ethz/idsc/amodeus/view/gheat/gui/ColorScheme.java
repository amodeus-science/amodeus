/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.gheat.gui;

import java.awt.Color;

public class ColorScheme {
    public static final int SIZE = 256;
    // ---
    private final Color[] colors = new Color[SIZE];

    /** @param index ranges from 0 to 255 inclusive
     * @param color */
    public void set(int index, Color color) {
        colors[index] = color;
    }

    /** @param index ranges from 0 to 255 inclusive
     * @return color corresponding to value i in range [0, 255] */
    public Color get(int index) {
        return colors[index];
    }
}
