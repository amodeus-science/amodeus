/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Color;

import ch.ethz.idsc.amodeus.analysis.plot.ColorDataAmodeusSpecific;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

/** color schemes for RoboTaxiStatus */
/* package */ enum RoboTaxiStatusColors {
    Standard(ColorDataAmodeusSpecific.STANDARD.cyclic()), //
    Mild(ColorDataAmodeusSpecific.MILD.cyclic()), //
    /*** New poppy color set */
    Pop(ColorDataAmodeusSpecific.POP.cyclic()), //
    ;

    private final ColorDataIndexed colorDataIndexed;
    private final ColorDataIndexed colorDataIndexedDest;

    private static final int ALPHA = 64;

    RoboTaxiStatusColors(ColorDataIndexed colorDataIndexed) {
        this.colorDataIndexed = colorDataIndexed;
        colorDataIndexedDest = colorDataIndexed.deriveWithAlpha(ALPHA);
    }

    public Color of(RoboTaxiStatus status) {
        return colorDataIndexed.getColor(status.ordinal());
    }

    public Color ofDest(RoboTaxiStatus status) {
        return colorDataIndexedDest.getColor(status.ordinal());
    }

}
