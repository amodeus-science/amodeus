/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;

/* package */ class VehicleStatistic {

    public final Tensor distanceTotal;
    public final Tensor distanceWithCustomer;
    public final Tensor distancePickup;
    public final Tensor distanceRebalance;

    private int lastLinkIndex = -1;
    private int offset = -1;
    // this is used as a buffer and is periodically emptied
    private final List<VehicleContainer> list = new LinkedList<>();

    public VehicleStatistic(int tics_max) {
        distanceTotal = Array.zeros(tics_max);
        distanceWithCustomer = Array.zeros(tics_max);
        distancePickup = Array.zeros(tics_max);
        distanceRebalance = Array.zeros(tics_max);
    }

    public void register(int tics, VehicleContainer vehicleContainer) {
        if (vehicleContainer.linkIndex != lastLinkIndex) {
            consolidate();
            list.clear();
            offset = tics;
            lastLinkIndex = vehicleContainer.linkIndex;
        }
        list.add(vehicleContainer);
    }

    public void consolidate() {
        if (!list.isEmpty()) {
            // here we are if the AV left the link. now we want to register the
            // distance covered by this vehicle
            final int linkId = list.get(0).linkIndex;
            Link currentLink = MatsimStaticDatabase.INSTANCE.getOsmLink(linkId).link;
            double distance = currentLink.getLength();

            int part = Math.toIntExact(list.stream().filter(vc -> vc.roboTaxiStatus.isDriving()).count());

            // Distance covered by one Vehiclecontainer
            Scalar contrib = RealScalar.of(distance / part);
            int count = 0;
            for (VehicleContainer vehicleContainer : list) {
                final int index = offset + count;
                if (index < distanceTotal.length()) {
                    switch (vehicleContainer.roboTaxiStatus) {
                    case DRIVEWITHCUSTOMER:
                        distanceWithCustomer.set(contrib, index);
                        distanceTotal.set(contrib, index); // applies to all three
                        break;
                    case DRIVETOCUSTOMER:
                        distancePickup.set(contrib, index);
                        distanceTotal.set(contrib, index); // applies to all three
                        break;
                    case REBALANCEDRIVE:
                        distanceRebalance.set(contrib, index);
                        distanceTotal.set(contrib, index); // applies to all three
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
