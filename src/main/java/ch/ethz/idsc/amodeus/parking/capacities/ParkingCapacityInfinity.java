/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.capacities;

import org.matsim.api.core.v01.network.Network;

public class ParkingCapacityInfinity extends ParkingCapacityAbstract {

    public ParkingCapacityInfinity(Network network) {
        network.getLinks().values().forEach(link -> capacities.put(link.getId(), Long.MAX_VALUE));
    }

}
