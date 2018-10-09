/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.awt.Color;

import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum ColorScheme {
    NONE(ColorDataListAmodeus._none.strict()), //
    STANDARD(ColorDataListAmodeus._standard.strict()), //
    POP(ColorDataListAmodeus._pop.strict()), //
    COLORFUL(ColorDataListAmodeus._colorfull.strict()), //
    PASSENGER(ColorDataListAmodeus._colorfull.strict()),//
    ;

    private final ColorDataIndexed colorDataIndexed;

    private ColorScheme(ColorDataIndexed colorDataIndexed) {
        this.colorDataIndexed = colorDataIndexed;
    }

    public Color of(int index) {
        return colorDataIndexed.getColor(index);
    }

}
