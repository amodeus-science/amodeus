/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacity;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;

/* package */ abstract class ParkingStrategyWithCapacity implements ParkingStrategy {

    protected ParkingCapacity parkingCapacity;
    protected Network network;
    protected DistanceFunction distanceFunction;

    /** this function gives the implementation the possibility to use the network and the distance function which will only be available after the construction.
     * Normally this function should be called in the constructor of the dispatcher.
     * 
     * @param network
     * @param distanceFunction */
    @Override
    public void setRunntimeParameters(ParkingCapacity avSpatialCapacityAmodeus, Network network, DistanceFunction distanceFunction) {
        this.parkingCapacity = avSpatialCapacityAmodeus;
        this.network = network;
        this.distanceFunction = distanceFunction;
    }
}
