package ch.ethz.idsc.amodeus.dispatcher.parking.strategies;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceFunction;

public interface ParkingStrategy {

    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis, Collection<RoboTaxi> rebalancingRobotaxis, long now);

    default public void setRunntimeParameters(Network network, DistanceFunction distanceFunction) {
        // -- empty by default. If somene wants to use these variables in a Startegy it has to be overwritten
    }
}
