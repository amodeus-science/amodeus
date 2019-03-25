/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

/** interface for distances in the AMoDeus simulations */
public interface DistanceFunction {

    /** @return distance from the {@link RoboTaxi} @param robotaxi to the {@link AVRequest}
     * @param avRequest */
    double getDistance(RoboTaxi robotaxi, AVRequest avRequest);

    /** @return distance from the {@link RoboTaxi} @param robotaxi to the {@link Link}
     * @param link */
    double getDistance(RoboTaxi robotaxi, Link link);

    /** @return distance between the {@link Link} @param from and the {@link Link}
     * @param to */
    double getDistance(Link from, Link to);
}
