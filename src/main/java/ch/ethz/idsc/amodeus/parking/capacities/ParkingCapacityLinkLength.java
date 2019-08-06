/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.capacities;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

public class ParkingCapacityLinkLength extends ParkingCapacityAbstract {

    /** Assigns to every link in the @param network a capacity equal to:
     * min(@param minCapacityGlobal, link lenghth x @param capacityPerLengthUnit ) */
    public ParkingCapacityLinkLength(Network network, double capacityPerLengthUnit, long minCapacityGlobal) {
        for (Link link : network.getLinks().values()) {
            long capacity = (long) Math.max(Math.floor(link.getLength() * capacityPerLengthUnit), minCapacityGlobal);
            capacities.put(link.getId(), capacity);
        }
    }

}
