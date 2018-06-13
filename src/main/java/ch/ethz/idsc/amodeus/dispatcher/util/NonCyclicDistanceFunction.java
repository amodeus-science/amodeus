/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Objects;

public class NonCyclicDistanceFunction extends EuclideanDistanceFunction {
    final DistanceFunction cyclicSolutionPreventer;

    public NonCyclicDistanceFunction(DistanceFunction cyclicSolutionPreventer) {
        this.cyclicSolutionPreventer = Objects.requireNonNull(cyclicSolutionPreventer);
    }

}
