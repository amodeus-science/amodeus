/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

/** abstract base class for all implementations that match {@link Collection}s of {@link RoboTaxi}s
 * and {@link Collection} of {@link AVRequest}s or of {@link Link}s */
public abstract class AbstractRoboTaxiDestMatcher {

    /** @return {@link Map} containing matching between {@link RoboTaxi}s in {@link Collection} @param roboTaxis
     *         and {@link AVRequest}s in @param avRequests or empty colletion if either is empty */
    public final Map<RoboTaxi, AVRequest> match(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> avRequests) {
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

    protected abstract Map<RoboTaxi, AVRequest> protected_match(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> avRequests);

    protected abstract Map<RoboTaxi, Link> protected_matchLink(Collection<RoboTaxi> roboTaxis, Collection<Link> links);

    protected abstract void updateCurrentTime(double now);
    
}
