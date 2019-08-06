/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.capacities;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

public class ParkingCapacityConstant extends ParkingCapacityAbstract {

    /** Assigns to every {@link Link} in the @param network a constant parking
     * capacity of @param capacity */
    public ParkingCapacityConstant(Network network, long capacity) {
        for (Link link : network.getLinks().values()) {
            capacities.put(link.getId(), capacity);
        }
    }

}
