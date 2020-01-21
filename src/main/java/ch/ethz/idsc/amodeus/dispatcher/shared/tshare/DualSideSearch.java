/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.matsim.av.passenger.AVRequest;

/** Implementation of the "Algorithm 1: Dual Side Taxi Searching" */
/* package */ class DualSideSearch {

    private final Map<VirtualNode<Link>, GridCell> gridCells;
    private final VirtualNetwork<Link> virtualNetwork;

    public DualSideSearch(Map<VirtualNode<Link>, GridCell> gridCells, VirtualNetwork<Link> virtualNetwork) {
        this.gridCells = gridCells;
        this.virtualNetwork = virtualNetwork;
    }

    public Collection<RoboTaxi> apply(AVRequest request, Map<VirtualNode<Link>, Set<RoboTaxi>> plannedLocations, //
            Scalar latestPickup, Scalar latestArrval) {

        /** origin cell = {@link GridCell} of {@link VirtualNode} containing the request origin link */
        GridCell oCell = gridCells.get(virtualNetwork.getVirtualNode(request.getFromLink()));
        /** destination cell = {@link GridCell} of {@link VirtualNode} containing the request destination link */
        GridCell dCell = gridCells.get(virtualNetwork.getVirtualNode(request.getToLink()));

        /** oCloseCells = cells reachable before latest pickup */
        Collection<VirtualNode<Link>> oCloseCells = oCell.nodesReachableWithin(latestPickup);
        /** dCloseCells = cells reachable before latest arrival */
        Collection<VirtualNode<Link>> dCloseCells = dCell.nodesReachableWithin(latestArrval);

        Collection<RoboTaxi> oTaxis = new ArrayList<>();
        Collection<RoboTaxi> dTaxis = new ArrayList<>();
        Collection<RoboTaxi> potentialTaxis = new ArrayList<>();

        boolean stop0 = false;
        boolean stopD = false;

        /** Loop finds potential taxis for which trip insertion is evaluated */
        int i0 = 0;
        int iD = 0;
        while (potentialTaxis.isEmpty() && (!stop0 || !stopD)) {
            if (i0 < oCloseCells.size()) {
                VirtualNode<Link> vNode = oCell.getVNodeAt(i0);
                if (oCloseCells.contains(vNode))
                    oTaxis.addAll(plannedLocations.get(vNode));
                ++i0;
            } else
                stop0 = true;
            if (iD < dCloseCells.size()) {
                VirtualNode<Link> vNode = dCell.getVNodeAt(iD);
                if (dCloseCells.contains(vNode))
                    dTaxis.addAll(plannedLocations.get(vNode));
                ++iD;
            } else
                stopD = true;
            potentialTaxis = CollectionUtils.intersection(oTaxis, dTaxis);
        }
        return potentialTaxis;
    }
}
