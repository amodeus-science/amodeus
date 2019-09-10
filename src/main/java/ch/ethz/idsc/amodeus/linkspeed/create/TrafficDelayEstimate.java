/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import ch.ethz.idsc.tensor.Tensor;

public interface TrafficDelayEstimate {

    /** @return traffic delays estimated with a flow matrix @param flowMatrix
     *         and a deviation from the free flow speed @param deviation */
    public Tensor compute(Tensor flowMatrix, Tensor deviation);

}
