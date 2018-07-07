/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/** helper functions predominantly used in HeuristicSharedDispatcher
 * 
 * @author Lukas Sieber */
enum StaticHelper {
    ;

    static double distanceRobotaxiRequest(AVRequest avRequest, RoboTaxi roboTaxi) {
        return NetworkUtils.getEuclideanDistance( //
                avRequest.getFromLink().getCoord(), //
                roboTaxi.getDivertableLocation().getCoord());
    }

    static Set<Link> getCloseLinks(Coord coord, double distance, Network network) {
        Collection<Node> closeNodes = NetworkUtils.getNearestNodes(network, coord, distance);
        GlobalAssert.that(!closeNodes.isEmpty());
        Set<Link> closeLinks = network.getLinks().values().stream() //
                .filter(link -> closeNodes.contains(link.getFromNode())) //
                .filter(link -> closeNodes.contains(link.getToNode())) //
                .collect(Collectors.toSet());
        GlobalAssert.that(!closeLinks.isEmpty());
        return closeLinks;
    }

    static RoboTaxi findClostestVehicle(AVRequest avRequest, Collection<RoboTaxi> roboTaxis) {
        GlobalAssert.that(roboTaxis != null);
        RoboTaxi closestRoboTaxi = null;
        double min = Double.POSITIVE_INFINITY;
        for (RoboTaxi roboTaxi : roboTaxis) {
            double newDistance = distanceRobotaxiRequest(avRequest, roboTaxi);
            if (closestRoboTaxi == null || newDistance < min) {
                min = newDistance;
                closestRoboTaxi = roboTaxi;
            }
        }
        return closestRoboTaxi;
    }

    static Map<Link, Set<AVRequest>> getFromLinkMap(Collection<AVRequest> avRequests) {
        Map<Link, Set<AVRequest>> linkAVRequestMap = new HashMap<>();
        for (AVRequest avRequest : avRequests) {
            Link fromLink = avRequest.getFromLink();
            if (!linkAVRequestMap.containsKey(fromLink))
                linkAVRequestMap.put(fromLink, new HashSet<>());
            linkAVRequestMap.get(fromLink).add(avRequest);
        }
        return linkAVRequestMap;
    }

}
