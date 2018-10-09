/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.awt.Color;

import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum ColorSchemeOld {
    NONE(ColorDataListAmodeusSpecific._none.strict()), //
    STANDARD(ColorDataListAmodeusSpecific._standard.strict()), //
    POP(ColorDataListAmodeusSpecific._pop.strict()), //
    COLORFUL(ColorDataListAmodeusSpecific._colorfull.strict()), //
    PASSENGER(ColorDataListAmodeusSpecific._colorfull.strict()),//
    ;

    private final ColorDataIndexed colorDataIndexed;

    private ColorSchemeOld(ColorDataIndexed colorDataIndexed) {
        this.colorDataIndexed = colorDataIndexed;
    }

    public Color of(int index) {
        return colorDataIndexed.getColor(index);
    }

}
