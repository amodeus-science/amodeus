/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.nd;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.red.VectorNormInterface;

public interface NdCenterInterface extends VectorNormInterface {
    /** @param center
     * @return */
    static NdCenterInterface euclidean(Tensor center) {
        return new EuclideanNdCenter(center);
    }

    /** @return center */
    Tensor center();
}