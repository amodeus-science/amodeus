/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.awt.Color;

import org.apache.commons.lang3.ArrayUtils;

public enum ColorScheme {
    NONE( // One being used right now
            new Color(255, 85, 85), // with customer
            new Color(85, 85, 255), // to customer
            new Color(85, 255, 85), // rebalance
            new Color(255, 255, 85), // stay
            new Color(255, 85, 255)), // off service
    STANDARD( // Standard colorScheme from Amodeus Viewer
            new Color(128, 0, 128), // with customer
            new Color(255, 51, 0), //
            new Color(0, 153, 255), //
            new Color(0, 204, 0), //
            new Color(64, 64, 64)), // off service
    POP( // Poppy colorScheme from Amodeus Viewer
            new Color(255, 0, 0), // with customer
            new Color(255, 192, 0), //
            new Color(0, 224, 255), //
            new Color(0, 255, 0), //
            new Color(32, 32, 32)), // off service
    COLORFUL( // Colorful theme matching the RoboTaxiStatii
            new Color(255, 0, 0), // with customer
            new Color(255, 192, 0), //
            new Color(0, 0, 255), //
            new Color(0, 255, 0), //
            new Color(224, 224, 224)), // off service
    LONG( // One being used right now
            new Color(255, 85, 85), // with customer
            new Color(85, 85, 255), // to customer
            new Color(85, 255, 85), // rebalance
            new Color(255, 255, 85), // stay
            new Color(255, 85, 255), // TODO LUkas Fill with meaningfull colors
            new Color(255, 90, 255), new Color(255, 95, 255), new Color(255, 100, 255), new Color(255, 105, 255), new Color(255, 110, 255), new Color(255, 115, 255),
            new Color(255, 120, 255), new Color(255, 125, 255), new Color(255, 130, 255), new Color(255, 135, 255), new Color(255, 140, 255), new Color(255, 145, 255),
            new Color(255, 150, 255), new Color(255, 155, 255), new Color(255, 160, 255), new Color(255, 165, 255), new Color(255, 170, 255), new Color(255, 175, 255),
            new Color(255, 180, 255), new Color(255, 185, 255), new Color(255, 160, 255), new Color(255, 165, 255), new Color(255, 170, 255), new Color(255, 175, 255),
            new Color(255, 180, 255), new Color(255, 185, 255), new Color(255, 190, 255), new Color(255, 195, 255), new Color(255, 200, 255), new Color(255, 205, 255)), // off
                                                                                                                                                                         // service
    ;

    private final Color[] colors;

    private ColorScheme(Color... colors) {
        this.colors = colors;
    }

    public Color of(int index) {
        return colors[index];
    }

    // TODO find better way of doing something like this
    @Deprecated
    public void reverseFirstN(int n) {
        Color[] firstCols = ArrayUtils.subarray(colors, 0, n + 1);
        ArrayUtils.reverse(firstCols);
        for (int i = 0; i < firstCols.length; i++) {
            colors[i] = firstCols[i];
        }
    }
}
