/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import org.matsim.api.core.v01.network.Network;

@FunctionalInterface
public interface ParkingCapacityGenerator {

    /** Generates a {@link ParkingCapacityAmodeus} for a given Network. This could for example be by searching for a given link attribute in the network or by
     * using the length of the link as a indication of its capacity.
     * 
     * @param network The Network for which the {@link ParkingCapacityAmodeus} should be generated
     * @return */
    public ParkingCapacityAmodeus generate(Network network);

}
