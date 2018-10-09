package ch.ethz.idsc.amodeus.analysis.plot;

import java.awt.Color;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.ColorDataLists;

public enum ColorDataListsAmodeus implements ColorDataIndexed{
    NONE(ColorDataListAmodeusSpecific._none.strict()), //
    COLORFULL(ColorDataListAmodeusSpecific._colorfull.strict()), //
    STANDARD(ColorDataListAmodeusSpecific._standard.strict()), //
    POP(ColorDataListAmodeusSpecific._pop.strict()), //
    MILD(ColorDataListAmodeusSpecific._mild.strict()), //
    LONG(ColorDataLists._097.cyclic())
    ;
    
    private final ColorDataIndexed colorDataIndexed;

    private ColorDataListsAmodeus(ColorDataIndexed colorDataIndexed) {
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
