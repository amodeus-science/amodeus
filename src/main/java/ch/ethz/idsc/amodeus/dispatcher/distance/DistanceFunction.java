/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.distance;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

public interface DistanceFunction {
    double getDistance(RoboTaxi robotaxi, AVRequest avRequest);

    double getDistance(RoboTaxi robotaxi, Link link);

    double getDistance(Link from, Link to);
}
