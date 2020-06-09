/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.parking.strategies;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.parking.capacities.ParkingCapacity;
import amodeus.amodeus.routing.DistanceFunction;

/** {@link ParkingStrategy} is the routine which defines how parking space capacity violations
 * are tried to be avoided. */
public interface ParkingStrategy {

    /** Function which should take action to keep Parking spaces free and stop
     * overfilling of the parking spaces, it's normally called in the redispatch
     * function of a dispatcher.
     * 
     * @param stayRoboTaxis
     * @param rebRoboTaxis
     * @param now
     * @return */
    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayRoboTaxis, Collection<RoboTaxi> rebRoboTaxis, long now);

    /** This function gives the implementation the possibility to use the avSpatialCapacity,
     * the network and the distance function which will only be available after the construction.
     * Normally this function should be called in the constructor of the dispatcher.
     * 
     * @param parkingCapacity
     * @param network
     * @param distanceFunction */
    public void setRuntimeParameters(ParkingCapacity parkingCapacity, Network network, DistanceFunction distanceFunction);

}
