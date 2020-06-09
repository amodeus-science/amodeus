/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.parking.capacities;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

public class ParkingCapacityConstant implements ParkingCapacity {
    private final long capacity;
    private final Network network;

    /** Assigns to every {@link Link} in the @param network a constant parking
     * capacity of @param capacity */
    public ParkingCapacityConstant(Network network, long capacity) {
        this.capacity = capacity;
        this.network = network;
    }

    @Override
    public long getSpatialCapacity(Id<Link> id) {
        return capacity;
    }

    @Override
    public Collection<Id<Link>> getAvailableLinks() {
        if (capacity > 0) // all links
            return network.getLinks().keySet();
        return new ArrayList<>();
    }
}
