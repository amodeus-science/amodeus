/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.UnitCapRoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

public interface DistanceFunction {
    double getDistance(UnitCapRoboTaxi robotaxi, AVRequest avRequest);

    double getDistance(UnitCapRoboTaxi robotaxi, Link link);

    double getDistance(Link from, Link to);
}
