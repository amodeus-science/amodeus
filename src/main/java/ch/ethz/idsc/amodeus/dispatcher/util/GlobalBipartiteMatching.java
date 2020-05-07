/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

/** Use to solve a {@link RoboTaxi} to {@link AVRequest} assignment problem via
 * global (all to all) bipartite matching. Solved using
 * the Hungarian algorithm */
public class GlobalBipartiteMatching extends AbstractRoboTaxiDestMatcher {

    protected final GlobalBipartiteCost globalBipartiteCost;

    /** Set the matching cost.
     * 
     * @param globalBipartiteCost - implementation of the GlobalBipartiteCost functional interface */
    public GlobalBipartiteMatching(GlobalBipartiteCost globalBipartiteCost) {
        this.globalBipartiteCost = Objects.requireNonNull(globalBipartiteCost);
    }

    /** Match RoboTaxis to AVRequests
     * 
     * @param roboTaxis
     * @param requests
     * @return */
    @Override
    protected Map<RoboTaxi, AVRequest> protected_match(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests) {
        return GlobalBipartiteHelper.genericMatch(roboTaxis, requests, AVRequest::getFromLink, globalBipartiteCost);
    }

    /** Match Robotaxis to links.
     * 
     * @param roboTaxis
     * @param links
     * @return */
    @Override
    protected Map<RoboTaxi, Link> protected_matchLink(Collection<RoboTaxi> roboTaxis, Collection<Link> links) {
        return GlobalBipartiteHelper.genericMatch(roboTaxis, links, link -> link, globalBipartiteCost);
    }

}
