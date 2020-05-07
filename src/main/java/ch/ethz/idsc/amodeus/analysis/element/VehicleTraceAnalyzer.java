/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.qty.Unit;

// TODO the linkTrace and timeTrace lists could be partially emptied during the register 
// steps to save memory, this should be done in the long-term
/* package */ class VehicleTraceAnalyzer {
    private final MatsimAmodeusDatabase db;
    private final Unit unit;

    // --
    private final List<Integer> linkTrace = new ArrayList<>();
    private final List<Long> timeTrace = new ArrayList<>();

    // --
    /* package */ Scalar vehicleTotalDistance;
    /* package */ Scalar vehicleCustomerDist;
    /* package */ Scalar vehiclePickupDist;
    /* package */ Scalar vehicleRebalancedist;
    /* package */ NavigableMap<Long, Scalar> distanceAtTime;
    /* package */ NavigableMap<Long, RoboTaxiStatus> statusAtTime = new TreeMap<Long, RoboTaxiStatus>();

    public VehicleTraceAnalyzer(MatsimAmodeusDatabase db) {
        this.db = db;
        unit = db.referenceFrame.unit();
        vehicleCustomerDist = Quantity.of(0, unit);
        vehiclePickupDist = Quantity.of(0, unit);
        vehicleRebalancedist = Quantity.of(0, unit);
    }

    public void register(VehicleContainer vc, long now) {
        // recording link and time trace
        for (int linkID : vc.linkTrace)
            if (linkTrace.size() == 0 || linkTrace.get(linkTrace.size() - 1) != linkID) {
                linkTrace.add(linkID);
                timeTrace.add(now);
            }

        // recording status at time
        statusAtTime.put(now, vc.roboTaxiStatus);
    }

    /* package */ void consolidate() {
        /** compute distance at every time step */
        GlobalAssert.that(linkTrace.size() == timeTrace.size());
        distanceAtTime = new TreeMap<Long, Scalar>();
        distanceAtTime.put((long) 0, Quantity.of(0, unit));
        for (int i = 1; i < linkTrace.size(); ++i)
            if (linkTrace.get(i - 1) != linkTrace.get(i)) { // link has changed
                Scalar distanceLink = Quantity.of(db.getOsmLink(linkTrace.get(i - 1)).link.getLength(), unit);
                Scalar distanceBefore = distanceAtTime.lastEntry().getValue();
                distanceAtTime.put(timeTrace.get(i - 1), distanceBefore.add(distanceLink));
            }

        /** compute total distances */
        vehicleTotalDistance = distanceAtTime.lastEntry().getValue();
        Entry<Long, Scalar> prevEntry = null;
        for (Entry<Long, Scalar> entry : distanceAtTime.entrySet()) {
            if (Objects.nonNull(prevEntry)) {
                Scalar dist = entry.getValue().subtract(prevEntry.getValue());
                RoboTaxiStatus status = statusAtTime.get(entry.getKey());
                switch (status) {
                case DRIVEWITHCUSTOMER:
                    vehicleCustomerDist = vehicleCustomerDist.add(dist);
                    break;
                case DRIVETOCUSTOMER:
                    vehiclePickupDist = vehiclePickupDist.add(dist);
                    break;
                case REBALANCEDRIVE:
                    vehicleRebalancedist = vehicleRebalancedist.add(dist);
                    break;
                default:
                    break;
                }
            }
            prevEntry = entry;
        }
    }

    /** @return at time step {@link Long} @param time1 encoded
     *         as {total distance, with customer,pickup,rebalance} */
    /* package */ Tensor labeledIntervalDistance(Long time1, Long time2) {
        Scalar distance = distanceInInterval(time1, time2);
        RoboTaxiStatus status = statusAtTime.floorEntry(time1).getValue();
        if (status.equals(RoboTaxiStatus.DRIVEWITHCUSTOMER))
            return Tensors.of(distance, distance, RealScalar.ZERO, RealScalar.ZERO);
        if (status.equals(RoboTaxiStatus.DRIVETOCUSTOMER))
            return Tensors.of(distance, RealScalar.ZERO, distance, RealScalar.ZERO);
        if (status.equals(RoboTaxiStatus.REBALANCEDRIVE))
            return Tensors.of(distance, RealScalar.ZERO, RealScalar.ZERO, distance);
        return Tensors.of(distance, RealScalar.ZERO, RealScalar.ZERO, RealScalar.ZERO);
    }

    /* package */ Scalar distanceInInterval(Long timeLow, Long timeHigh) {
        // get lower and ceiling entry
        Entry<Long, Scalar> entryLower = distanceAtTime.lowerEntry(timeLow);
        Entry<Long, Scalar> entryHigher = Objects.nonNull(distanceAtTime.ceilingEntry(timeHigh)) ? //
                distanceAtTime.ceilingEntry(timeHigh) : //
                entryLower;

        // compute distance at timeLow and timeHigh with linear interpolation
        Scalar dLow = VehicleTraceHelper.distanceAt(timeLow, entryLower, entryHigher);
        Scalar dHigh = VehicleTraceHelper.distanceAt(timeHigh, entryLower, entryHigher);

        GlobalAssert.that(Scalars.lessEquals(dLow, vehicleTotalDistance));
        GlobalAssert.that(Scalars.lessEquals(dHigh, vehicleTotalDistance));

        // return the difference
        return dHigh.subtract(dLow);
    }
}