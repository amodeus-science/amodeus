/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import org.matsim.api.core.v01.network.Network;

public interface AVSpatialCapacityGenerator {
    public AVSpatialCapacityAmodeus generate(Network network);
}
