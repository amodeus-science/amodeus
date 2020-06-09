/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.routing.EuclideanDistanceFunction;

/** Euclidean distance implementation for bipartite matching assignment in dispatcher */
public enum EuclideanDistanceCost implements GlobalBipartiteCost {
    INSTANCE;

    @Override
    public double between(RoboTaxi roboTaxi, Link link) {
        return EuclideanDistanceFunction.INSTANCE.getDistance(roboTaxi, link);
    }
}
