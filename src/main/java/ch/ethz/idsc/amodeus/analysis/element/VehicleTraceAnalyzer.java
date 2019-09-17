/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;

/* package */ class VehicleTraceAnalyzer {

    private final MatsimAmodeusDatabase db;

    public final Tensor stepDistanceTotal;
    public final Tensor stepDistanceWithCustomer;
    public final Tensor stepDistancePickup;
    public final Tensor stepDistanceRebalance;

    private int lastLinkIndex = -1;
    private int simObjIndLastLinkChange = -1;
    /** This is used as a buffer and is periodically emptied: */
    private final List<VehicleContainer> list = new LinkedList<>();

    public VehicleTraceAnalyzer(int stepsMax, MatsimAmodeusDatabase db) {
        this.db = db;
        stepDistanceTotal = Array.zeros(stepsMax);
        stepDistanceWithCustomer = Array.zeros(stepsMax);
        stepDistancePickup = Array.zeros(stepsMax);
        stepDistanceRebalance = Array.zeros(stepsMax);
    }

    public void register(int simObjIndex, VehicleContainer vc) {
        if (vc.linkTrace[0] != lastLinkIndex) {
            consolidate();
            list.clear();
            simObjIndLastLinkChange = simObjIndex;
            lastLinkIndex = vc.linkTrace[vc.linkTrace.length - 1];
        }
        list.add(vc);
    }

    /** this function is called when the {@link RoboTaxi} has changed the link, then the can
     * distance covered by the vehicle on the previous link can be registered and associated to
     * time steps. The logic is that the distance is added evenly to the time steps, as the
     * queuing based simulation model does not allow for more detailed information. */
    /* package */ void consolidate() {
        if (!list.isEmpty()) {

            /** this total distance on the link was traveled on during all
             * simulationObjects stored in the list. */

            // NEW ====
            double distance = 0;
            for (int linkId : list.get(0).linkTrace) {
                Link distanceLink = db.getOsmLink(linkId).link;
                distance += distanceLink.getLength();
            }

            // BEFORE ====
            // final int linkId = list.get(0).linkTrace[list.get(0).linkTrace.length - 1];
            // Link distanceLink = db.getOsmLink(linkId).link;
            // double distance = distanceLink.getLength();

            int part = Math.toIntExact(list.stream().filter(vc -> vc.roboTaxiStatus.isDriving()).count());

            /** distance covered by one {@link VehicleContainer} */
            Scalar stepDistcontrib = RealScalar.of(distance / part);
            int count = 0;
            for (VehicleContainer vehicleContainer : list) {
                final int index = simObjIndLastLinkChange + count;
                if (index < stepDistanceTotal.length()) {
                    switch (vehicleContainer.roboTaxiStatus) {
                    case DRIVEWITHCUSTOMER:
                        stepDistanceWithCustomer.set(stepDistcontrib, index);
                        stepDistanceTotal.set(stepDistcontrib, index); // applies to all three
                        break;
                    case DRIVETOCUSTOMER:
                        stepDistancePickup.set(stepDistcontrib, index);
                        stepDistanceTotal.set(stepDistcontrib, index); // applies to all three
                        break;
                    case REBALANCEDRIVE:
                        stepDistanceRebalance.set(stepDistcontrib, index);
                        stepDistanceTotal.set(stepDistcontrib, index); // applies to all three
                        break;
                    default:
                        break;
                    }
                }
                ++count;
            }
        }
    }
}
