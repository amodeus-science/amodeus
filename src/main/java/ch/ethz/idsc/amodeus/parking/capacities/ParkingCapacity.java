/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.capacities;

import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

/** A {@link ParkingCapacity} is a storage for the spatial capacity
 * (i.e. parking spaces) on a Collection of links (e.g. the network). */
public interface ParkingCapacity {

    /** An AVSpatialCapacity gives back the Number of Spaces on the Link with
     * the given Id back. It is designed for the usage of parking spaces such
     * that for each link the corresponding parking capacity can be found
     * 
     * @param id link id
     * @return the total capacity on this link */
    public long getSpatialCapacity(Id<Link> id);

    public Collection<Id<Link>> getAvailableLinks();
}
