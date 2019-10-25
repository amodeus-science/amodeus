/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.math;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.sca.Sign;

public enum PositiveSubVector {
    ;

    // TODO document why this function is useful?
    /** @param vector
     * @return */
    public static Tensor of(Tensor vector) {
        return Tensor.of(vector.flatten(-1) //
                .map(Scalar.class::cast) //
                .filter(Sign::isPositiveOrZero));

    }
}
