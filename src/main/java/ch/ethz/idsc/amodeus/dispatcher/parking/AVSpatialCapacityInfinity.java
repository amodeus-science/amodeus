/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import org.matsim.api.core.v01.network.Network;

public class AVSpatialCapacityInfinity extends AVSpatialCapacityAbstract {

    public AVSpatialCapacityInfinity(Network network) {
        network.getLinks().values().forEach(link -> capacities.put(link.getId(), Long.MAX_VALUE));
    }

}
