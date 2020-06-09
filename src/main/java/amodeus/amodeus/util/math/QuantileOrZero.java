/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.math;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Quantile;
import ch.ethz.idsc.tensor.sca.ScalarUnaryOperator;

public enum QuantileOrZero {
    ;

    public static ScalarUnaryOperator of(Tensor submission) {
        if (Tensors.isEmpty(submission))
            return s -> RealScalar.ZERO;
        return Quantile.of(submission);
    }
}
