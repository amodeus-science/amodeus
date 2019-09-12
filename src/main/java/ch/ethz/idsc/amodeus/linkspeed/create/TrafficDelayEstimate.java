/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import ch.ethz.idsc.tensor.Tensor;

public interface TrafficDelayEstimate {

    /** @return a {@link Tensor} containing the delays for every link/time segment, i.e.,
     *         the additional travel time compared to the free flow travel time. The number is computed
     *         based on the @param deviation recorded in the data. The @param flowMatrix is a matrix of
     *         dimension nxm containing only 0 and 1. For every of the m recorded trips, it encodes
     *         on which of the n link/time segments the trip passes. */
    public Tensor compute(Tensor flowMatrix, Tensor deviation);

}
