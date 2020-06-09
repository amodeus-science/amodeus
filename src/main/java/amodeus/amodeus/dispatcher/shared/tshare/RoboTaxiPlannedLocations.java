/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.tshare;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.shared.SharedCourse;
import amodeus.amodeus.dispatcher.shared.SharedMenu;
import amodeus.amodeus.virtualnetwork.core.VirtualLink;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import amodeus.amodeus.virtualnetwork.core.VirtualNode;

/** According to details provided in the publication, for each {@link VirtualNode} taxis
 * must be known that enter the node within a certain time span. This is approximated by
 * choosing the current {@link VirtualNode} of the {@link RoboTaxi}, all of the nodes it will
 * visit based on its {@link SharedMenu}, and all neighboring {@link VirtualNode}s of these nodes. */
/* package */ enum RoboTaxiPlannedLocations {
    ;

    public static Map<VirtualNode<Link>, Set<RoboTaxi>> of(Collection<RoboTaxi> passengerCarrying, //
            VirtualNetwork<Link> virtualNetwork) {

        /** For each {@link VirtualNode}, the {@link RoboTaxi} which
         * are going to enter it are stored in this {@link Map} */
        Map<VirtualNode<Link>, Set<RoboTaxi>> locationMap = new HashMap<>();
        for (VirtualNode<Link> virtualNode : virtualNetwork.getVirtualNodes())
            locationMap.put(virtualNode, new HashSet<>());

        /** No potential vehicles for sharing --> return empty map */
        if (passengerCarrying.size() == 0)
            return locationMap;

        /** for all {@link RoboTaxi}s add to list */
        for (RoboTaxi roboTaxi : passengerCarrying) {

            /** get all nodes corresponding to present or to-be-visited locations */
            Set<VirtualNode<Link>> relevantVNodes = new HashSet<>();
            // add location
            relevantVNodes.add(virtualNetwork.getVirtualNode(roboTaxi.getDivertableLocation()));
            // add location of all planned courses
            for (SharedCourse course : roboTaxi.getUnmodifiableViewOfCourses())
                relevantVNodes.add(virtualNetwork.getVirtualNode(course.getLink()));

            /** get current location and add */
            Set<VirtualNode<Link>> allNeighbors = new HashSet<>();
            for (VirtualNode<Link> vNode : relevantVNodes)
                allNeighbors.addAll(fromAndToNeighbors(vNode, virtualNetwork));

            /** add all to initial set */
            relevantVNodes.addAll(allNeighbors);

            /** add roboTaxi to all identified nodes */
            relevantVNodes.forEach(vNode -> locationMap.get(vNode).add(roboTaxi));

        }
        return locationMap;
    }

    private static Collection<VirtualNode<Link>> fromAndToNeighbors(VirtualNode<Link> vNode, VirtualNetwork<Link> virtualNetwork) {
        /** get reachable to-neighbors, i.e., virtual nodes v for which a link
         * (vNode, v) exists */
        List<VirtualNode<Link>> toNeighbors = virtualNetwork.getVirtualLinks().stream() // get VirtualLinks
                .filter(vl -> vl.getFrom().equals(vNode)) // get from- and to- neighbors
                .map(VirtualLink::getTo).collect(Collectors.toList());

        /** get reachable from-neighbors, i.e., virtual nodes v for which a link
         * (v, vNode) exists */
        List<VirtualNode<Link>> fromNeighbors = virtualNetwork.getVirtualLinks().stream() // get VirtualLinks
                .filter(vl -> vl.getTo().equals(vNode)) // get from- and to- neighbors
                .map(VirtualLink::getFrom).collect(Collectors.toList());

        return CollectionUtils.intersection(toNeighbors, fromNeighbors);
    }
}
