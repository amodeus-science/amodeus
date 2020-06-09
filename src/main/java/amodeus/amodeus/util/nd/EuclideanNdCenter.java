/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.nd;

import java.io.Serializable;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.red.Norm;

/* package */ class EuclideanNdCenter implements NdCenterInterface, Serializable {
    private final Tensor center;

    public EuclideanNdCenter(Tensor center) {
        this.center = center.copy().unmodifiable();
    }

    @Override
    public Scalar ofVector(Tensor vector) {
        return Norm._2.between(vector, center);
    }

    @Override
    public Tensor center() {
        return center;
    }
}
