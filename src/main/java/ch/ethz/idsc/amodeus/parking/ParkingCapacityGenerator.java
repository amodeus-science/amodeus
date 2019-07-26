/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacity;

@FunctionalInterface
public interface ParkingCapacityGenerator {

    /** Generates a {@link ParkingCapacity} for a given Network.
     * This could for example be by searching for a given link attribute
     * in the network or by using the length of the link as a indication
     * of its capacity.
     * 
     * @param network The Network for which the {@link ParkingCapacity} should be generated
     * @return */
    public ParkingCapacity generate(Network network);

}
