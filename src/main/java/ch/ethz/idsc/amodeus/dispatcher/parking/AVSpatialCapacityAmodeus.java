/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public interface AVSpatialCapacityAmodeus {

    /** @param id
     * @return */
    // TODO document function
    long getSpatialCapacity(Id<Link> id);

}
