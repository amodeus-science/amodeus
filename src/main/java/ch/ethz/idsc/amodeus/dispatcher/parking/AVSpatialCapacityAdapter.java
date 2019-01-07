/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public abstract class AVSpatialCapacityAdapter implements AVSpatialCapacityAmodeus {

    /**
     * Storing all the given Capacities in a Map
     */
    protected final Map<Id<Link>, Long> capacities = new HashMap<>();

    /**
     * finds the stored Spatial Capacity for the given link.
     * @return the stored capacity or 0 if no value was found for the given link ID
     */
    @Override
    public long getSpatialCapacity(Id<Link> linkId) {
        return capacities.containsKey(linkId) //
                ? capacities.get(linkId)
                : 0;
    }

    public Collection<Id<Link>> getAvaiableLinks() {
        return capacities.keySet();
    }
}
