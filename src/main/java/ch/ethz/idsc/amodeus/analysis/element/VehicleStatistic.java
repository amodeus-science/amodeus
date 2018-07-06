/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.simobj.VehicleContainer;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;

/* package */ class VehicleStatistic {

    public final Tensor distanceTotal;
    public final Tensor distanceWithCustomer;
    public final Tensor distancePickup;
    public final Tensor distanceRebalance;

    private int lastLinkIndex = -1;
    private int simObjIndLastLinkChange = -1;
    private int lastUpdatedDist = 0;
    // this is used as a buffer and is periodically emptied
    private final List<VehicleContainer> list = new LinkedList<>();

    public VehicleStatistic(int tics_max) {
        distanceTotal = Array.zeros(tics_max);
        distanceWithCustomer = Array.zeros(tics_max);
        distancePickup = Array.zeros(tics_max);
        distanceRebalance = Array.zeros(tics_max);
    }

    public void register(int simObjIndex, VehicleContainer vehicleContainer) {
        if (vehicleContainer.linkIndex != lastLinkIndex){
            consolidate();
            list.clear();
            simObjIndLastLinkChange = simObjIndex;
            lastLinkIndex = vehicleContainer.linkIndex;
        }
        list.add(vehicleContainer);
    }

    /** this function is called when the {@link RoboTaxi} has changed the link, then we can
     * register the distance covered by the vehicle on the previous link and associate it to
     * timesteps. The logic is that the distance is added evenly to the time steps. */
    public void consolidate() {
        if (!list.isEmpty()) {
            final int linkId = list.get(0).linkIndex;
            Link distanceLink = MatsimStaticDatabase.INSTANCE.getOsmLink(linkId).link;
            /** this total distance on the link was travelled on during all simulationObjects stored
             * in the list. */
            double distance = distanceLink.getLength();

            int part = Math.toIntExact(list.stream().filter(vc -> vc.roboTaxiStatus.isDriving()).count());

            // Distance covered by one Vehiclecontainer
            Scalar stepDistcontrib = RealScalar.of(distance / part);
            int count = 0;
            for (VehicleContainer vehicleContainer : list) {
                final int index = simObjIndLastLinkChange + count;
                lastUpdatedDist = index;
                if (index < distanceTotal.length()) {
                    switch (vehicleContainer.roboTaxiStatus) {
                    case DRIVEWITHCUSTOMER:
                        distanceWithCustomer.set(stepDistcontrib, index);
                        distanceTotal.set(stepDistcontrib, index); // applies to all three
                        break;
                    case DRIVETOCUSTOMER:
                        distancePickup.set(stepDistcontrib, index);
                        distanceTotal.set(stepDistcontrib, index); // applies to all three
                        break;
                    case REBALANCEDRIVE:
                        distanceRebalance.set(stepDistcontrib, index);
                        distanceTotal.set(stepDistcontrib, index); // applies to all three
                        break;
                    default:
                        break;
                    }
                }
                ++count;
            }
        }
    }

    /** @return latest recording of Tensor {distanceTotal, distanceWithCustomer,distancePickup,distanceRebalancd} */
    public Tensor getLatestRecordings() {
        return Tensors.of( //
                distanceTotal.Get(lastUpdatedDist), //
                distanceWithCustomer.Get(lastUpdatedDist), //
                distancePickup.Get(lastUpdatedDist), //
                distanceRebalance.Get(lastUpdatedDist));
    }

}
