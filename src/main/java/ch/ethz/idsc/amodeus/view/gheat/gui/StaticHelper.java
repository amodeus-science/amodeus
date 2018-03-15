/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.gheat.gui;

import java.awt.Color;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Subdivide;
import ch.ethz.idsc.tensor.img.ColorFormat;
import ch.ethz.idsc.tensor.opt.ScalarTensorFunction;

/* package */ enum StaticHelper {
    ;
    public static ColorScheme forward(ScalarTensorFunction colorDataGradient) {
        return from(colorDataGradient, Subdivide.of(0, 1, ColorScheme.SIZE - 1));
    }

    public static ColorScheme reverse(ScalarTensorFunction colorDataGradient) {
        return from(colorDataGradient, Subdivide.of(1, 0, ColorScheme.SIZE - 1));
    }

    private static ColorScheme from(ScalarTensorFunction colorDataGradient, Tensor linspace) {
        ColorScheme colorScheme = new ColorScheme();
        GlobalAssert.that(linspace.length() == ColorScheme.SIZE);
        for (int index = 0; index < ColorScheme.SIZE; ++index) {
            Scalar scalar = linspace.Get(index);
            Tensor vector = colorDataGradient.apply(scalar);
            vector.set(RealScalar.of(255 - index), 3);
            Color color = ColorFormat.toColor(vector);
            colorScheme.set(index, color);
        }
        return colorScheme;
    }
}
