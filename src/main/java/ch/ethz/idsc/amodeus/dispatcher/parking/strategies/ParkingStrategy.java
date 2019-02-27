/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking.strategies;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceFunction;

/** A Parking Startegy is the routine which defines how Parking spaces are tried to keep free. */
public interface ParkingStrategy {

    /** Function which should take action to keep Parking spaces free and stopp overfilling of the parking spaces.
     * Its normally called in the redispatch function of a dispatcher.
     * 
     * @param stayingRobotaxis
     * @param rebalancingRobotaxis
     * @param now
     * @return */
    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis, Collection<RoboTaxi> rebalancingRobotaxis, long now);

    /** this function gives the implementation the possibility to use the network and the distance function which will only be available after the construction.
     * Normally this function should be called in the constructor of the dispatcher.
     * 
     * @param network
     * @param distanceFunction */
    default public void setRunntimeParameters(Network network, DistanceFunction distanceFunction) {
        // -- empty by default. If somene wants to use these variables in a Startegy it has to be overwritten
    }
}
