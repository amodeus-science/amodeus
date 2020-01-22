package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.red.Max;

/* package */ class DualSideRoutines {

    private List<Collection<RoboTaxi>> oTaxiOld = new ArrayList<>();
    private List<Collection<RoboTaxi>> dTaxiOld = new ArrayList<>();

    private List<Collection<RoboTaxi>> oTaxiNew = new ArrayList<>();
    private List<Collection<RoboTaxi>> dTaxiNew = new ArrayList<>();

    public void clear() {
        oTaxiOld.clear();
        oTaxiNew.clear();
        dTaxiOld.clear();
        dTaxiNew.clear();
    }

    public void print() {

        {
            System.out.println("oTaxis: ");
            int nOld = oTaxiOld.size();
            int nNew = oTaxiNew.size();
            int nMax = Math.max(nOld, nNew);

            for (int i = 0; i < nMax; ++i) {
                if (i < oTaxiOld.size())
                    StaticHelper.rtCollectionPrinter(oTaxiOld.get(i), "old",false);
                if (i < oTaxiNew.size())
                    StaticHelper.rtCollectionPrinter(oTaxiNew.get(i), "new",false);
            }
            System.out.println("---");
        }

        {
            System.out.println("dTaxis: ");
            int nOld = dTaxiOld.size();
            int nNew = dTaxiNew.size();
            int nMax = Math.max(nOld, nNew);

            for (int i = 0; i < nMax; ++i) {
                if (i < dTaxiOld.size())
                    StaticHelper.rtCollectionPrinter(dTaxiOld.get(i), "old",false);
                if (i < dTaxiNew.size())
                    StaticHelper.rtCollectionPrinter(dTaxiNew.get(i), "new",false);
            }
            System.out.println("---");
        }

    }

    public Collection<RoboTaxi> computeOld(//
            GridCell oCell, GridCell dCell, //
            Collection<VirtualNode<Link>> oCloseCells, //
            Collection<VirtualNode<Link>> dCloseCells, //
            Map<VirtualNode<Link>, Set<RoboTaxi>> plannedLocations) {

        HashSet<RoboTaxi> oTaxis = new HashSet<>();
        HashSet<RoboTaxi> dTaxis = new HashSet<>();
        Collection<RoboTaxi> potentialTaxis = new ArrayList<>();

        int i0 = 0;
        int iD = 0;
        boolean stop0 = false;
        boolean stopD = false;
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

            // StaticHelper.rtCollectionPrinter(oTaxis, "oTaxis");
            // StaticHelper.rtCollectionPrinter(dTaxis, "dTaxis");

            oTaxiOld.add(oTaxis);
            dTaxiOld.add(dTaxis);

        }
        return potentialTaxis;
    }

    public Collection<RoboTaxi> computeNew(//
            GridCell oCell, GridCell dCell, //
            Collection<VirtualNode<Link>> oCloseCells, //
            Collection<VirtualNode<Link>> dCloseCells, //
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

            // StaticHelper.rtCollectionPrinter(oTaxis, "oTaxis");
            // StaticHelper.rtCollectionPrinter(dTaxis, "dTaxis");

            oTaxiNew.add(oTaxis);
            dTaxiNew.add(dTaxis);

        }

        return potentialTaxis;

    }
}
