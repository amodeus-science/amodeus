/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.matsim.av.passenger.AVRequest;

/** Implementation of "Algorithm 1: Dual Side Taxi Searching" */
/* package */ class DualSideSearch {

    private final Map<VirtualNode<Link>, GridCell> gridCells;
    private final VirtualNetwork<Link> virtualNetwork;

    public DualSideSearch(Map<VirtualNode<Link>, GridCell> gridCells, VirtualNetwork<Link> virtualNetwork) {
        this.gridCells = gridCells;
        this.virtualNetwork = virtualNetwork;
    }

    public Collection<RoboTaxi> apply(AVRequest request, Map<VirtualNode<Link>, Set<RoboTaxi>> plannedLocations, //
            Scalar timeLeftForPickup, Scalar timeLeftUntilArrival) {

        /** origin cell = {@link GridCell} of {@link VirtualNode} containing the request origin link */
        GridCell oCell = gridCells.get(virtualNetwork.getVirtualNode(request.getFromLink()));
        /** destination cell = {@link GridCell} of {@link VirtualNode} containing the request destination link */
        GridCell dCell = gridCells.get(virtualNetwork.getVirtualNode(request.getToLink()));

        /** oCloseCells = cells reachable before latest pickup */
        Collection<VirtualNode<Link>> oCloseCells = oCell.nodesReachableWithin(timeLeftForPickup);
        /** dCloseCells = cells reachable before latest arrival */
        Collection<VirtualNode<Link>> dCloseCells = dCell.nodesReachableWithin(timeLeftUntilArrival);

        /** compute set of potential taxis for ride sharing with dual side search */
        return dualSideSearch(oCell, dCell, oCloseCells, dCloseCells, plannedLocations);
    }

    private Collection<RoboTaxi> dualSideSearch(GridCell oCell, GridCell dCell, //
            Collection<VirtualNode<Link>> oCloseCells, Collection<VirtualNode<Link>> dCloseCells, //
            Map<VirtualNode<Link>, Set<RoboTaxi>> plannedLocations) {

        HashSet<RoboTaxi> oTaxis = new HashSet<>();
        HashSet<RoboTaxi> dTaxis = new HashSet<>();
        Collection<RoboTaxi> potentialTaxis = new ArrayList<>();

        /** Loop finds potential taxis for which trip insertion is evaluated */
        int index = 0;
        boolean stop0 = false;
        boolean stopD = false;
        while (potentialTaxis.isEmpty() && (!stop0 || !stopD)) {

            /** iterate neighbors according to closedness, get taxis if
             * within reachable time for pickup */
            if (0 < oCloseCells.size()) {
                VirtualNode<Link> vNode = oCell.getVNodeAt(index);
                if (oCloseCells.contains(vNode)) {
                    oTaxis.addAll(plannedLocations.get(vNode));
                    oCloseCells.remove(vNode);
                }
            } else
                stop0 = true;
            /** iterate neighbors according to closedness, get taxis if
             * within reachable time for pickup */
            if (0 < dCloseCells.size()) {
                VirtualNode<Link> vNode = dCell.getVNodeAt(index);
                if (dCloseCells.contains(vNode)) {
                    dTaxis.addAll(plannedLocations.get(vNode));
                    dCloseCells.remove(vNode);
                }
            } else
                stopD = true;
            /** compute intersection */
            potentialTaxis = CollectionUtils.intersection(oTaxis, dTaxis);
            /** increase index */
            ++index;
        }
        return potentialTaxis;
    }
}
