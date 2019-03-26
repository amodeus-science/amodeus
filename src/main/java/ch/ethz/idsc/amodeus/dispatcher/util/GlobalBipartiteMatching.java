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

    private final DistanceFunction distanceFunction;

    public GlobalBipartiteMatching(DistanceFunction distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    @Override
    protected Map<RoboTaxi, AVRequest> protected_match(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests) {
        GlobalBipartiteWeight specificWeight = new GlobalBipartiteWeight() {
            @Override
            public double between(RoboTaxi roboTaxi, Link link) {
                return distanceFunction.getDistance(roboTaxi, link);
            }
        };
        return GlobalBipartiteHelper.genericMatch(roboTaxis, requests, AVRequest::getFromLink, specificWeight);
    }

    @Override
    protected Map<RoboTaxi, Link> protected_matchLink(Collection<RoboTaxi> roboTaxis, Collection<Link> links) {
        GlobalBipartiteWeight specificWeight = new GlobalBipartiteWeight() {
            @Override
            public double between(RoboTaxi roboTaxi, Link link) {
                return distanceFunction.getDistance(roboTaxi, link);
            }
        };
        return GlobalBipartiteHelper.genericMatch(roboTaxis, links, link -> link, specificWeight);
    }
}
