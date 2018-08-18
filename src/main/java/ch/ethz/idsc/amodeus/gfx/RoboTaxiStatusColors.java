/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Color;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;

/** color schemes for RoboTaxiStatus */
public enum RoboTaxiStatusColors {
    Standard( //
            new Color(128, 0, 128), // with customer
            new Color(255, 51, 0), // to customer
            new Color(0, 153, 255), // rebalance
            new Color(0, 204, 0), // stay
            new Color(64, 64, 64)), // off service
    Mild( //
            new Color(92, 0, 92), //
            new Color(224, 32, 0), //
            new Color(0, 128, 224), //
            new Color(0, 128, 0, 64), //
            new Color(128, 128, 128)), // off service
    /*** New poppy color set */
    Pop( //
            new Color(255, 0, 0), // with customer
            new Color(255, 192, 0), // to customer
            new Color(0, 224, 255), // rebalance
            new Color(0, 255, 0), // stay
            new Color(32, 32, 32)), // off service
    ;

    private final Color[] colors;
    private final Color[] dest;

    private RoboTaxiStatusColors(Color... colors) {
        this.colors = colors;
        dest = new Color[colors.length];
        for (int index = 0; index < colors.length; ++index)
            dest[index] = ofDest(colors[index]);
    }

    public Color of(RoboTaxiStatus status) {
        return colors[status.ordinal()];
    }

    public Color ofDest(RoboTaxiStatus status) {
        return dest[status.ordinal()];
    }

    // helper function
    private static Color ofDest(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 64);
    }
}
