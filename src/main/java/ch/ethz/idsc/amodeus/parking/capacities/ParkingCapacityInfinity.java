/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.capacities;

import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

public class ParkingCapacityInfinity implements ParkingCapacity {
    private final Network network;

    public ParkingCapacityInfinity(Network network) {
        this.network = network;
    }

    @Override
    public long getSpatialCapacity(Id<Link> id) {
        return Long.MAX_VALUE;
    }

    @Override
    public Collection<Id<Link>> getAvailableLinks() {
        return network.getLinks().keySet();
    }
}
