/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.hungarian.HungarianAlgorithmWrap;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/** array matching with Euclidean distance as criteria */

public class GlobalBipartiteMatchingMatcher extends AbstractVehicleDestMatcher {

    private final DistanceFunction distancer;

    public GlobalBipartiteMatchingMatcher(DistanceFunction distancer) {
        this.distancer = distancer;

    }

    @Override
    protected Map<RoboTaxi, AVRequest> protected_matchAVRequest(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests) {

        Collection<MatchLinkObject<AVRequest>> linksGen = new ArrayList<>();
        requests.stream().forEach(l -> linksGen.add(new MatchLinkObject<>(l)));
        return genericMatch(roboTaxis, linksGen);
    }

    @Override
    protected Map<RoboTaxi, Link> protected_matchLink(Collection<RoboTaxi> roboTaxis, Collection<Link> links) {

        Collection<MatchLinkObject<Link>> linksGen = new ArrayList<>();
        links.stream().forEach(l -> linksGen.add(new MatchLinkObject<>(l)));
        return genericMatch(roboTaxis, linksGen);
    }

    private <T> Map<RoboTaxi, T> genericMatch(Collection<RoboTaxi> roboTaxis, Collection<MatchLinkObject<T>> linkObjects) {

        // since Collection::iterator does not make guarantees about the order we store the pairs in a list
        final List<RoboTaxi> orderedRoboTaxis = new ArrayList<>(roboTaxis);
        final List<MatchLinkObject<T>> ordered_linkObjects = new ArrayList<>(linkObjects);

        // cost of assigning vehicle i to dest j, i.e. distance from vehicle i to destination j
        final int n = orderedRoboTaxis.size(); // workers
        final int m = ordered_linkObjects.size(); // jobs

        final double[][] distancematrix = new double[n][m];

        int i = -1;
        for (RoboTaxi roboTaxi : orderedRoboTaxis) {
            ++i;
            int j = -1;
            for (MatchLinkObject<T> linkObj : ordered_linkObjects) {
                distancematrix[i][++j] = distancer.getDistance(roboTaxi, linkObj.getLink());
            }
        }

        // vehicle at position i is assigned to destination matchinghungarianAlgorithm[j]
        // int[] matchinghungarianAlgorithm = new HungarianAlgorithm(distancematrix).execute(); // O(n^3)
        int[] matchinghungarianAlgorithm = HungarianAlgorithmWrap.matching(distancematrix);

        // do the assignment according to the Hungarian algorithm (only for the matched elements, otherwise keep current drive destination)
        final Map<RoboTaxi, T> map = new HashMap<>();
        i = -1;
        for (RoboTaxi roboTaxi : orderedRoboTaxis) {
            ++i;
            if (0 <= matchinghungarianAlgorithm[i]) {
                map.put(roboTaxi, ordered_linkObjects.get(matchinghungarianAlgorithm[i]).getObject());
            }
        }

        GlobalAssert.that(map.size() == Math.min(n, m));
        return map;

    }

}
