/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/** abstract base class for all implementations that match {@link Collection}s of {@link RoboTaxi}s
 * and {@link Collection} of {@link PassengerRequest}s or of {@link Link}s */
public abstract class AbstractRoboTaxiDestMatcher {

    /** @return {@link Map} containing matching between {@link RoboTaxi}s in {@link Collection} @param roboTaxis
     *         and {@link PassengerRequest}s in @param avRequests or empty colletion if either is empty */
    public final Map<RoboTaxi, PassengerRequest> match(Collection<RoboTaxi> roboTaxis, Collection<PassengerRequest> avRequests) {
        if (roboTaxis.isEmpty() || avRequests.isEmpty())
            return Collections.emptyMap();
        return protected_match(roboTaxis, avRequests);
    }

    /** @return {@link Map} containing matching between {@link RoboTaxi}s in {@link Collection} @param roboTaxis
     *         and {@link Link}s in @param links or empty colletion if either is empty */
    public final Map<RoboTaxi, Link> matchLink(Collection<RoboTaxi> roboTaxis, Collection<Link> links) {
        if (roboTaxis.isEmpty() || links.isEmpty())
            return Collections.emptyMap();
        return protected_matchLink(roboTaxis, links);
    }

    protected abstract Map<RoboTaxi, PassengerRequest> protected_match(Collection<RoboTaxi> roboTaxis, Collection<PassengerRequest> avRequests);

    protected abstract Map<RoboTaxi, Link> protected_matchLink(Collection<RoboTaxi> roboTaxis, Collection<Link> links);

}
