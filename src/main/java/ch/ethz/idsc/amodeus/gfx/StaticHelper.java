/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import ch.ethz.idsc.amodeus.dispatcher.core.RStatusHelper;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.NormalizeUnlessZero;
import ch.ethz.idsc.tensor.opt.TensorUnaryOperator;
import ch.ethz.idsc.tensor.red.Max;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Median;
import ch.ethz.idsc.tensor.red.Norm;

/* package */ enum StaticHelper {
    ;
    private static final TensorUnaryOperator NORMALIZE = NormalizeUnlessZero.with(Norm._1);

    public static Scalar meanOrZero(Tensor vector) {
        if (Tensors.isEmpty(vector))
            return RealScalar.ZERO;
        return Mean.of(vector).Get();
    }

    public static Scalar medianOrZero(Tensor vector) {
        if (vector.length() == 0)
            return RealScalar.ZERO;
        return Median.of(vector).Get();
    }

    public static Scalar maxOrZero(Tensor vector) {
        return vector.flatten(0) //
                .map(Scalar.class::cast) //
                .reduce(Max::of).orElse(RealScalar.ZERO);
    }

    /** @param vector with non-negative entries
     * @return */
    public static Tensor normalize1Norm224(Tensor vector) {
        if (Tensors.isEmpty(vector))
            return Tensors.empty();
        return NORMALIZE.apply(vector).multiply(RealScalar.of(224));
    }

    public static boolean isWaiting(RequestContainer requestContainer) {
        return RStatusHelper.unserviced(requestContainer.requestStatus);
    }
}
