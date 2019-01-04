/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public class AVSpatialCapacityAdapter implements AVSpatialCapacityAmodeus {

    protected final Map<Id<Link>, Long> capacities = new HashMap<>();

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
