/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.capacities;

import java.util.Random;

import org.matsim.api.core.v01.network.Network;

public class ParkingCapacityFromNetworkIdentifier extends ParkingCapacityFromNetworkDistribution {

    /** Reads the spatial capacities from the Network file.
     * If the given tag for the spatialCapacity is found its
     * corresponding value is taken for the capacity.
     * 
     * @param network
     * @param spatialAvCapacityString */
    public ParkingCapacityFromNetworkIdentifier(Network network, String spatialAvCapacityString, //
            Random random) {
        super(network, spatialAvCapacityString, random, Long.MAX_VALUE);
    }
}
