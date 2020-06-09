/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.routing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.RoboTaxi;

/** interface for distances in the AMoDeus simulations */
public interface DistanceFunction {

    /** @return distance from the {@link RoboTaxi} @param robotaxi to the {@link PassengerRequest}
     * @param avRequest */
    double getDistance(RoboTaxi roboTaxi, PassengerRequest avRequest);

    /** @return distance from the {@link RoboTaxi} @param robotaxi to the {@link Link}
     * @param link */
    double getDistance(RoboTaxi roboTaxi, Link link);

    /** @return distance between the {@link Link} @param from and the {@link Link}
     * @param to */
    double getDistance(Link from, Link to);
}
