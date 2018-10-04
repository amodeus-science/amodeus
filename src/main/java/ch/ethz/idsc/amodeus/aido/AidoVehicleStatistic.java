/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

// TODO small rounding errors compared to VehicleStatistic
// ... find out where the differences come from and adapt
/* package */ class AidoVehicleStatistic {
    private final MatsimAmodeusDatabase db;

    public AidoVehicleStatistic(MatsimAmodeusDatabase db) {
        this.db = db;
    }

    /** list is used as a buffer and is periodically emptied */
    private final List<VehicleContainer> list = new LinkedList<>();
    private int lastLinkIndex = -1;

    /** @param vehicleContainer
     * @return vector of length 2, entries have unit "m" */
    Tensor distance(VehicleContainer vehicleContainer) {
        Tensor distance = StaticHelper.ZEROS.copy();
        if (vehicleContainer.linkIndex != lastLinkIndex) {
            distance = consolidate();
            list.clear();
            lastLinkIndex = vehicleContainer.linkIndex;
        }
        list.add(vehicleContainer);
        return distance;
    }

    /** this function is called when the {@link RoboTaxi} has changed the link, then we can
     * register the distance covered by the vehicle on the previous link and associate it to
     * timesteps. The logic is that the distance is added evenly to the time steps.
     * 
     * @return vector of length 2, entries have unit "m" */
    public Tensor consolidate() {
        Scalar distDrive = Quantity.of(0, SI.METER);
        Scalar distEmpty = Quantity.of(0, SI.METER);
        if (!list.isEmpty()) {
            final int linkId = list.get(0).linkIndex;
            Link distanceLink = db.getOsmLink(linkId).link;
            /** this total distance on the link was travelled on during all simulationObjects stored
             * in the list. */
            Scalar distance = Quantity.of(distanceLink.getLength(), SI.METER);

            int part = Math.toIntExact(list.stream().filter(vc -> vc.roboTaxiStatus.isDriving()).count());
            Scalar stepDistcontrib = distance.divide(RationalScalar.of(part, 1));

            for (VehicleContainer vehicleContainer : list) {
                switch (vehicleContainer.roboTaxiStatus) {
                case DRIVEWITHCUSTOMER:
                    distDrive = distDrive.add(stepDistcontrib);
                    break;
                case DRIVETOCUSTOMER:
                    distEmpty = distEmpty.add(stepDistcontrib);
                    break;
                case REBALANCEDRIVE:
                    distEmpty = distEmpty.add(stepDistcontrib);
                    break;
                default:
                    break;
                }
            }
        }
        return Tensors.of(distDrive, distEmpty);
    }
}
