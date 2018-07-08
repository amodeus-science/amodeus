/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Normalize;
import ch.ethz.idsc.tensor.red.Max;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Median;
import ch.ethz.idsc.tensor.red.Norm;

/* package */ enum StaticHelper {
    ;
    static Scalar meanOrZero(Tensor vector) {
        if (Tensors.isEmpty(vector))
            return RealScalar.ZERO;
        return Mean.of(vector).Get();
    }

    static Scalar medianOrZero(Tensor vector) {
        if (vector.length() == 0)
            return RealScalar.ZERO;
        return Median.of(vector).Get();
    }

    static Scalar maxOrZero(Tensor vector) {
        return vector.flatten(0) //
                .map(Scalar.class::cast) //
                .reduce(Max::of).orElse(RealScalar.ZERO);
    }

    /** @param vector with non-negative entries
     * @return */
    static Tensor normalize1Norm(Tensor vector) {
        if (Tensors.isEmpty(vector))
            return Tensors.empty();
        return Normalize.unlessZero(vector, Norm._1).multiply(RealScalar.of(224));
    }

    static boolean isWaiting(RequestContainer requestContainer) {
        return requestContainer.requestStatus.unServiced();
    }
}
