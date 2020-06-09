/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.math;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.ScalarQ;
import ch.ethz.idsc.tensor.Tensor;

public enum TotalAll {
    ;
    /** @param tensor
     * @return sum of all scalars in given tensor, or zero if tensor
     * @throws Exception if tensor is of type {@link Scalar} */
    public static Scalar of(Tensor tensor) {
        ScalarQ.thenThrow(tensor);
        return tensor.flatten(-1) //
                .map(Scalar.class::cast) //
                .reduce(Scalar::add) //
                .orElse(RealScalar.ZERO);
    }
}
