/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.parking.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.parking.capacities.ParkingCapacity;
import amodeus.amodeus.routing.DistanceFunction;

/** default {@link ParkingStrategy} that not doing any action. */
/* package */ class ParkingNullStrategy implements ParkingStrategy {

    @Override
    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis, //
            Collection<RoboTaxi> rebalancingRobotaxis, long now) {
        return new HashMap<>();
    }

    @Override
    public void setRuntimeParameters(ParkingCapacity avSpatialCapacityAmodeus, Network network, //
            DistanceFunction distanceFunction) {
        // ---
    }
}
