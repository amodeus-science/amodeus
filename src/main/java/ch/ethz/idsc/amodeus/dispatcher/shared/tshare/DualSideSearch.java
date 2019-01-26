/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.matsim.av.passenger.AVRequest;

public class DualSideSearch {

    private final Map<VirtualNode<Link>, GridCell> gridCells;
    private final VirtualNetwork<Link> virtualNetwork;

    public DualSideSearch(Map<VirtualNode<Link>, GridCell> gridCells, VirtualNetwork<Link> virtualNetwork, Network network) {
        this.virtualNetwork = virtualNetwork;
        this.gridCells = gridCells;
    }

    public Collection<RoboTaxi> apply(AVRequest request, Map<VirtualNode<Link>, Set<RoboTaxi>> plannedLocations, //
            Scalar latestPickup, Scalar latestArrval) {

        GridCell oCell = gridCells.get(virtualNetwork.getVirtualNode(request.getToLink()));
        GridCell dCell = gridCells.get(virtualNetwork.getVirtualNode(request.getFromLink()));

        Collection<RoboTaxi> oTaxis = new ArrayList<>();
        Collection<RoboTaxi> dTaxis = new ArrayList<>();
        Collection<RoboTaxi> potentialTaxis = new ArrayList<>();

        Collection<VirtualNode<Link>> oCloseCells = oCell.nodesReachableWithin(latestPickup);
        Collection<VirtualNode<Link>> dCloseCells = dCell.nodesReachableWithin(latestArrval);

        boolean stop0 = false;
        boolean stopD = false;

        int i0 = 0;
        int iD = 0;
        while (potentialTaxis.isEmpty() && (stop0 == false || stopD == false)) {

            if (i0 < oCloseCells.size()) {
                VirtualNode<Link> vNode = oCell.getDistAt(i0);
                if (oCloseCells.contains(vNode)) {
                    oTaxis.addAll(plannedLocations.get(vNode));
                }
                ++i0;
            } else
                stop0 = true;

            if (iD < dCloseCells.size()) {
                VirtualNode<Link> vNode = dCell.getDistAt(iD);
                if (dCloseCells.contains(vNode)) {
                    dTaxis.addAll(plannedLocations.get(vNode));
                }
                ++iD;
            } else
                stopD = true;
            potentialTaxis = Intersection.of(oTaxis, dTaxis);
        }
        return potentialTaxis;
    }
}
