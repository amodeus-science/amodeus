/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.math;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Mean;

public enum MeanOrZero {
    ;

    public static Scalar of(Tensor vector) {
        return Tensors.isEmpty(vector) //
                ? RealScalar.ZERO
                : (Scalar) Mean.of(vector);
    }
}
