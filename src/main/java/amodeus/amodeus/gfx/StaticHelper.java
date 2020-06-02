/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.gfx;

import amodeus.amodeus.dispatcher.core.RStatusHelper;
import amodeus.amodeus.net.RequestContainer;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Max;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Median;

/* package */ enum StaticHelper {
    ;

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

    public static boolean isWaiting(RequestContainer requestContainer) {
        return RStatusHelper.unserviced(requestContainer.requestStatus);
    }

}
