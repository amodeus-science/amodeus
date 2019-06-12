/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.matsim.av.passenger.AVRequest;

/** perform a global bipartite matching of {@link RoboTaxi} and {@link AVRequest}
 * or {@link Link} using the Hungarian Method */
public class GlobalBipartiteMatching extends AbstractRoboTaxiDestMatcher {

    protected final GlobalBipartiteWeight specificWeight;

    public GlobalBipartiteMatching(DistanceFunction distanceFunction) {
        this.specificWeight = new GlobalBipartiteWeight() {
            @Override
            public double between(RoboTaxi roboTaxi, Link link) {
                return distanceFunction.getDistance(roboTaxi, link);
            }
        };
    }

    @Override
    protected Map<RoboTaxi, AVRequest> protected_match(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests) {
        return GlobalBipartiteHelper.genericMatch(roboTaxis, requests, AVRequest::getFromLink, specificWeight);
    }

    @Override
    protected Map<RoboTaxi, Link> protected_matchLink(Collection<RoboTaxi> roboTaxis, Collection<Link> links) {
        return GlobalBipartiteHelper.genericMatch(roboTaxis, links, link -> link, specificWeight);
    }

    @Override
    protected void updateCurrentTime(double now) {
        // nothing to do here
    }

}
