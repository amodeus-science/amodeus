/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.awt.Color;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.ColorDataLists;

// TODO make a map that lookups CDI
public enum ColorDataAmodeus implements ColorDataIndexed {
    NONE(ColorDataAmodeusSpecific.NONE.strict()), //
    COLORFUL(ColorDataAmodeusSpecific.COLORFUL.strict()), //
    STANDARD(ColorDataAmodeusSpecific.STANDARD.strict()), //
    POP(ColorDataAmodeusSpecific.POP.strict()), //
    MILD(ColorDataAmodeusSpecific.MILD.strict()), //
    LONG(ColorDataLists._097.cyclic());

    private final ColorDataIndexed colorDataIndexed;

    private ColorDataAmodeus(ColorDataIndexed colorDataIndexed) {
        this.colorDataIndexed = colorDataIndexed;
    }

    @Override
    public Tensor apply(Scalar t) {
        return colorDataIndexed.apply(t);
    }

    @Override
    public Color getColor(int index) {
        return colorDataIndexed.getColor(index);
    }

    @Override
    public Color rescaled(double value) {
        return colorDataIndexed.rescaled(value);
    }

    @Override
    public ColorDataIndexed deriveWithAlpha(int alpha) {
        return colorDataIndexed.deriveWithAlpha(alpha);
    }
}
