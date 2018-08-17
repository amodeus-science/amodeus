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
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/** helper functions predominantly used in HeuristicSharedDispatcher */
/* package */ enum StaticHelper {
    ;

    public static Set<Link> getCloseLinks(Coord coord, double distance, Network network) {
        Collection<Node> closeNodes = NetworkUtils.getNearestNodes(network, coord, distance);
        GlobalAssert.that(!closeNodes.isEmpty());
        Set<Link> closeLinks = network.getLinks().values().stream() //
                .filter(link -> closeNodes.contains(link.getFromNode())) //
                .filter(link -> closeNodes.contains(link.getToNode())) //
                .collect(Collectors.toSet());
        GlobalAssert.that(!closeLinks.isEmpty());
        return closeLinks;
    }

    /** @param networkBounds
     * @return closest {@link AVRequest} @param avRequest to the {@link Collection} of available {@link RoboTaxi}s @param roboTaxis */
    public static RoboTaxi findClostestVehicle(AVRequest avRequest, Collection<RoboTaxi> roboTaxis, double networkBounds[]) {
        QuadTree<RoboTaxi> unassignedVehiclesTree = new QuadTree<>(networkBounds[0], networkBounds[1], networkBounds[2], networkBounds[3]);
        roboTaxis.stream().forEach(rt -> {
            Coord loc = rt.getDivertableLocation().getCoord();
            unassignedVehiclesTree.put(loc.getX(), loc.getY(), rt);
        });
        Coord loc = avRequest.getFromLink().getCoord();
        RoboTaxi closestRoboTaxi = unassignedVehiclesTree.getClosest(loc.getX(), loc.getY());
        return closestRoboTaxi;
    }

    public static Map<Link, Set<AVRequest>> getFromLinkMap(Collection<AVRequest> avRequests) {
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
