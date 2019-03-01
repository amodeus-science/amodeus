/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualLink;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;

/** According to details provided in the publication, for each {@link VirtualNode} taxis
 * must be known that enter the node within a certain time span. This is approximated by
 * choosing the current {@link VirtualNode} of the {@link RoboTaxi} as well as all
 * {@link VirtualNode}s connected to them. As the search space is continuously increased
 * in the {@link DualSideSearch} that accesses this function, it was decided against
 * implementing a computationally intenisve method in which the path of each {@link RoboTaxi}
 * is taken into account. */
/* package */ enum RoboTaxiPlannedLocations {
    ;

    public static Map<VirtualNode<Link>, Set<RoboTaxi>> of(Collection<RoboTaxi> customerCarrying, //
            VirtualNetwork<Link> virtualNetwork) {

        /** initialize list */
        Map<VirtualNode<Link>, Set<RoboTaxi>> locationMap = new HashMap<>();
        for (VirtualNode<Link> virtualNode : virtualNetwork.getVirtualNodes())
            locationMap.put(virtualNode, new HashSet<>());

        /** no potential vehicles for sharing */
        if (customerCarrying.size() == 0)
            return locationMap;

        /** for all {@link RoboTaxi}s add to list */
        for (RoboTaxi roboTaxi : customerCarrying) {
            Link loc = roboTaxi.getDivertableLocation();

            /** get current location and add */
            VirtualNode<Link> vNode = virtualNetwork.getVirtualNode(loc);
            locationMap.get(vNode).add(roboTaxi);

            /** get reachable neighbors */
            List<VirtualNode<Link>> neighbors = virtualNetwork.getVirtualLinks().stream()//
                    .filter(vl -> vl.getFrom().equals(vNode))//
                    .map(VirtualLink::getTo).collect(Collectors.toList());
            neighbors.stream().forEach(vn -> locationMap.get(vn).add(roboTaxi));
        }

        return locationMap;
    }
}
