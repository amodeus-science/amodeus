/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.capacities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public abstract class ParkingCapacityAbstract //
        implements ParkingCapacity {

    private boolean cleanupFlag = false;

    /** Storing all the given Capacities in a Map */
    protected final Map<Id<Link>, Long> capacities = new HashMap<>();

    /** finds the stored Spatial Capacity for the given link.
     * 
     * @return the stored capacity or 0 if no value was found for the given link ID */
    @Override
    public long getSpatialCapacity(Id<Link> linkId) {
        return capacities.containsKey(linkId) //
                ? capacities.get(linkId)
                : 0;
    }

    /** @return {@link Collection} of all {@link Id<Link>}s which
     *         have capacity > 0 */
    @Override
    public Collection<Id<Link>> getAvailableLinks() {
        // TODO find more elegant solution
        /** at first query, ensure that all links with capacity zero are
         * removed from the set. */
        if (!cleanupFlag) {
            Map<Id<Link>, Long> zeroCapacities = new HashMap<>();
            capacities.entrySet().forEach(e -> {
                if (e.getValue() < 1)
                    zeroCapacities.put(e.getKey(), e.getValue());
            });
            zeroCapacities.keySet().forEach(k -> capacities.remove(k));
            cleanupFlag = true;
        }
        return capacities.keySet();
    }
}
