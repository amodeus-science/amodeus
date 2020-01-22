/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.measure.spi.SystemOfUnits;

import org.apache.commons.collections.CollectionUtils;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.sca.Round;
import ch.ethz.matsim.av.passenger.AVRequest;

/** Implementation of "Algorithm 1: Dual Side Taxi Searching" */
/* package */ class DualSideSearch {

    private final Map<VirtualNode<Link>, GridCell> gridCells;
    private final VirtualNetwork<Link> virtualNetwork;

    private final DualSideRoutines dual = new DualSideRoutines();

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

        // System.out.println("Computing potentialTaxis1: ");
        dual.clear();
        Collection<RoboTaxi> potentialTaxis1 = //
                dual.computeOld(oCell, dCell, //
                        oCloseCells, dCloseCells, plannedLocations);
        // System.out.println("---");

        // System.out.println("Computing potentialTaxis2: ");
        Collection<RoboTaxi> potentialTaxis2 = //
                dual.computeNew(oCell, dCell, //
                        oCloseCells, dCloseCells, plannedLocations);
        // dual.print();
        // System.out.println("---");

        if (potentialTaxis1.size() != 0 || potentialTaxis2.size() != 0) {
            if (!CollectionUtils.isEqualCollection(potentialTaxis1, potentialTaxis2)) {
                StaticHelper.rtCollectionPrinter(potentialTaxis1, "PotentialTaxis1",true);
                StaticHelper.rtCollectionPrinter(potentialTaxis2, "PotentialTaxis2",true);
            }
        }

        return potentialTaxis1;
    }
}

// // NEW VERSION -------------------------------------------------------------------
// /** Loop finds potential taxis for which trip insertion is evaluated */
// int index = 0;
// boolean stop0 = false;
// boolean stopD = false;
// while (potentialTaxis.isEmpty() && (!stop0 || !stopD)) {
//
// System.out.println("oCloseCells.size: " + oCloseCells.size());
// System.out.println("dCloseCells.size: " + dCloseCells.size());
//
// /** iterate neighbors according to closedness, get taxis if
// * within reachable time for pickup */
// if (0 < oCloseCells.size()) {
// VirtualNode<Link> vNode = oCell.getVNodeAt(index);
// if (oCloseCells.contains(vNode)) {
// oTaxis.addAll(plannedLocations.get(vNode));
// oCloseCells.remove(vNode);
// }
// } else
// stop0 = true;
// /** iterate neighbors according to closedness, get taxis if
// * within reachable time for pickup */
// if (0 < dCloseCells.size()) {
// VirtualNode<Link> vNode = dCell.getVNodeAt(index);
// if (dCloseCells.contains(vNode)) {
// dTaxis.addAll(plannedLocations.get(vNode));
// dCloseCells.remove(vNode);
// }
// } else
// stopD = true;
// /** compute intersection */
// potentialTaxis = CollectionUtils.intersection(oTaxis, dTaxis);
// /** increase index */
// ++index;
// }
// // NEW VERSION END ------------------------------------------------------------

// OLD VERSION ----------------------------------------------------------------
// int i0 = 0;
// int iD = 0;
// boolean stop0 = false;
// boolean stopD = false;
// while (potentialTaxis.isEmpty() && (!stop0 || !stopD)) {
// if (i0 < oCloseCells.size()) {
// VirtualNode<Link> vNode = oCell.getVNodeAt(i0);
// if (oCloseCells.contains(vNode))
// oTaxis.addAll(plannedLocations.get(vNode));
// ++i0;
// } else
// stop0 = true;
// if (iD < dCloseCells.size()) {
// VirtualNode<Link> vNode = dCell.getVNodeAt(iD);
// if (dCloseCells.contains(vNode))
// dTaxis.addAll(plannedLocations.get(vNode));
// ++iD;
// } else
// stopD = true;
// potentialTaxis = CollectionUtils.intersection(oTaxis, dTaxis);
// }
// iOs.append(RealScalar.of(i0));
// iDs.append(RealScalar.of(iD));
// OLD VERSION END ------------------------------------------------------------