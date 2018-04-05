/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.awt.Color;

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
    ;

    private final Color[] colors;

    private ColorScheme(Color... colors) {
        this.colors = colors;
    }

    public Color of(int index) {
        return colors[index];
    }
}
