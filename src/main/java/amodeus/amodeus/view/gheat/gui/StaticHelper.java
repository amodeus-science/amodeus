/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.view.gheat.gui;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Subdivide;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.StrictColorDataIndexed;
import ch.ethz.idsc.tensor.opt.ScalarTensorFunction;

/* package */ enum StaticHelper {
    ;
    private static final int SIZE = 256;

    public static ColorDataIndexed forward(ScalarTensorFunction colorDataGradient) {
        return from(colorDataGradient, Subdivide.of(0, 1, SIZE - 1));
    }

    public static ColorDataIndexed reverse(ScalarTensorFunction colorDataGradient) {
        return from(colorDataGradient, Subdivide.of(1, 0, SIZE - 1));
    }

    private static ColorDataIndexed from(ScalarTensorFunction colorDataGradient, Tensor linspace) {
        Tensor matrix = Tensors.reserve(SIZE);
        for (int index = 0; index < SIZE; ++index) {
            Scalar scalar = linspace.Get(index);
            Tensor vector = colorDataGradient.apply(scalar);
            vector.set(RealScalar.of(255 - index), 3);
            matrix.append(vector);
        }
        return StrictColorDataIndexed.of(matrix);
    }
}
