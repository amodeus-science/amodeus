/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.parking.capacities;

import java.util.Random;

import org.matsim.api.core.v01.network.Network;

public class ParkingCapacityNetworkIdentifier extends ParkingCapacityNetworkDistribution {

    /** Reads the spatial capacities from the Network file.
     * If the given tag for the spatialCapacity is found its
     * corresponding value is taken for the capacity.
     * 
     * @param network
     * @param capacityString */
    public ParkingCapacityNetworkIdentifier(Network network, //
            String capacityString, //
            Random random) {
        super(network, capacityString, random, Long.MAX_VALUE);
    }
}
