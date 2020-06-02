/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.fifs;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import amodeus.amodeus.dispatcher.core.RoboTaxi;

/** Helper class to wrap the three elements Travel Time, Block and roboTaxi */
/* package */ class ShortestTrip {
    final double travelTime;
    final Block block;
    final RoboTaxi roboTaxi;

    ShortestTrip(Entry<Double, Map<Block, Set<RoboTaxi>>> nearestTrips) {
        this.travelTime = nearestTrips.getKey();
        this.block = nearestTrips.getValue().keySet().iterator().next();
        this.roboTaxi = nearestTrips.getValue().get(block).iterator().next();
    }
}