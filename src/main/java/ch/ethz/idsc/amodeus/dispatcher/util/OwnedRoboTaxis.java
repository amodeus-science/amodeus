/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;

/** A {@link VirtualNode} owns a {@link RoboTaxi} if the roboTaxi is in one of the following conditions:
 * - STAY status and on a link inside the virtualNode
 * - REBALANCE status and has a drive destination inside the virtualNode
 *
 * @author clruch */
public class OwnedRoboTaxis {
    private final VirtualNetwork<Link> virtualNetwork;
    private Map<VirtualNode<Link>, List<RoboTaxi>> owned;

    public OwnedRoboTaxis(VirtualNetwork<Link> virtualNetwork) {
        this.virtualNetwork = virtualNetwork;
        owned = virtualNetwork.getVirtualNodes().stream().collect(Collectors.toMap(vn -> vn, vn -> new ArrayList<>()));
    }

    public void update(List<RoboTaxi> allTaxis) {
        /** empty previously saved results */
        owned.values().forEach(List::clear);

        /** staying taxis */
        allTaxis.stream().filter(RoboTaxi::isInStayTask).forEach(rt -> //
        owned.get(virtualNetwork.getVirtualNode(rt.getDivertableLocation())).add(rt));

        /** rebalancing taxis */
        allTaxis.stream().filter(rt -> rt.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE)).forEach(rt -> //
        owned.get(virtualNetwork.getVirtualNode(rt.getCurrentDriveDestination())).add(rt));
    }

    public List<RoboTaxi> in(VirtualNode<Link> virtualNode) {
        return Collections.unmodifiableList(owned.get(virtualNode));
    }
}
