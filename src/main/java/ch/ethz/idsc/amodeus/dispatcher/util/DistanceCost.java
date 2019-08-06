/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;

public class DistanceCost implements GlobalBipartiteCost {
    private final DistanceFunction distanceFunction;

    public DistanceCost(DistanceFunction distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    @Override
    public double between(RoboTaxi roboTaxi, Link link) {
        return distanceFunction.getDistance(roboTaxi, link);
    }

}
