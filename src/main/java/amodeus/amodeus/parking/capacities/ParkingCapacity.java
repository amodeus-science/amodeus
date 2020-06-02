/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.parking.capacities;

import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

/** A {@link ParkingCapacity} is a storage for the spatial capacity
 * (i.e. parking spaces) on a {@link Collection} of {@link Link}s */
public interface ParkingCapacity {

    /** @return the total capacity on this link with {@link Id<Link>} @param id.
     *         An {@link ParkingCapacity} returns the number of spaces on the Link with
     *         the given Id back. It is designed for the usage of parking spaces such
     *         that for each link the corresponding parking capacity can be found */
    long getSpatialCapacity(Id<Link> id);

    /** @return {@link Collection} of all {@link Id<Link>} that have nonzero
     *         parking capacity, i.e., in {1,2,3,...} parking spots */
    Collection<Id<Link>> getAvailableLinks();
}
