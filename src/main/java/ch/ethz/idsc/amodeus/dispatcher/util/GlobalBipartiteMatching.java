/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/** Use to solve a {@link RoboTaxi} to {@link PassengerRequest} assignment problem via
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

    /** Match RoboTaxis to PassengerRequests
     * 
     * @param roboTaxis
     * @param requests
     * @return */
    @Override
    protected Map<RoboTaxi, PassengerRequest> protected_match(Collection<RoboTaxi> roboTaxis, Collection<PassengerRequest> requests) {
        return GlobalBipartiteHelper.genericMatch(roboTaxis, requests, PassengerRequest::getFromLink, globalBipartiteCost);
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
