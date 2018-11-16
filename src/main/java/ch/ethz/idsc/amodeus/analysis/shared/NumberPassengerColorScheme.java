/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.shared;

import java.awt.Color;

import ch.ethz.idsc.amodeus.analysis.plot.ColorDataAmodeusSpecific;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.img.ColorDataGradients;
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
        return (RealScalar.ONE.subtract(Exp.of(scalar.negate().divide(RealScalar.of(SCALINGFACTOR)))));
    }

    public static void main(String[] args) {
        NumberPassengerColorScheme test = new NumberPassengerColorScheme(ColorDataGradients.CLASSIC, ColorDataAmodeusSpecific.STANDARD.cyclic());

        System.out.println("Passenger: 0");
        System.out.println(test.of(RealScalar.of(0)));
        System.out.println("Passenger: 1");
        System.out.println(test.of(RealScalar.of(1)));
        System.out.println("Passenger: 2");
        System.out.println(test.of(RealScalar.of(2)));
        System.out.println("Passenger: 3");
        System.out.println(test.of(RealScalar.of(3)));
        System.out.println("Passenger: 4");
        System.out.println(test.of(RealScalar.of(4)));
        System.out.println("Passenger: 5");
        System.out.println(test.of(RealScalar.of(5)));
        System.out.println("Passenger: 6");
        System.out.println(test.of(RealScalar.of(6)));
        System.out.println("Passenger: 7");
        System.out.println(test.of(RealScalar.of(7)));
        System.out.println("Passenger: 8");
        System.out.println(test.of(RealScalar.of(8)));
        System.out.println("Passenger: 10");
        System.out.println(test.of(RealScalar.of(10)));
        System.out.println("Passenger: 100");

        System.out.println(test.of(RealScalar.of(100)));
        System.out.println("Passenger: 1000");
        System.out.println(test.of(RealScalar.of(1000)));

    }

}
