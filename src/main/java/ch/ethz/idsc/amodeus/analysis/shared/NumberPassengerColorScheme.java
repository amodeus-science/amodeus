/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.shared;

import java.awt.Color;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.ColorFormat;
import ch.ethz.idsc.tensor.opt.ScalarTensorFunction;
import ch.ethz.idsc.tensor.sca.Exp;

public class NumberPassengerColorScheme {

    private final ScalarTensorFunction colorDataGradient;
    private final ColorDataIndexed statusColorScheme;
    private static final double SCALINGFACTOR = 4;

    public NumberPassengerColorScheme(ScalarTensorFunction colorDataGradient, ColorDataIndexed statusColorScheme) {
        this.colorDataGradient = colorDataGradient;
        this.statusColorScheme = statusColorScheme;
    }

    public Color of(RoboTaxiStatus roboTaxiStatus) {
        return statusColorScheme.getColor(roboTaxiStatus.ordinal());
    }

    public Color of(Scalar numberPassengers) {
        Scalar fraction = functionTofraction(numberPassengers);
        // System.out.println("Fraction: "+fraction);
        Tensor vector = colorDataGradient.apply(fraction);
        vector.set(RealScalar.of(255), 3);
        return ColorFormat.toColor(vector);
    }

    private static Scalar functionTofraction(Scalar scalar) {
        return RealScalar.ONE.subtract(Exp.of(scalar.negate().divide(RealScalar.of(SCALINGFACTOR))));
    }

}
