/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.qty.Unit;
import ch.ethz.idsc.tensor.sca.ScalarUnaryOperator;

/* package */ class VehicleTraceAnalyzer {
    private final MatsimAmodeusDatabase db;
    private final Unit unit;

    // --
    private final List<Integer> linkTrace = new ArrayList<>();
    private final List<Long> timeTrace = new ArrayList<>();

    // --
    /* package */ Scalar vehicleTotalDistance;
    /* package */ Scalar vehicleCustomerDistance;
    /* package */ Scalar vehiclePickupDistance;
    /* package */ Scalar vehicleRebalancedistance;
    /* package */ NavigableMap<Long, Scalar> distanceAtTime;
    /* package */ NavigableMap<Long, RoboTaxiStatus> statusAtTime = new TreeMap<Long, RoboTaxiStatus>();

    // public final Tensor stepDistanceTotal;
    // public final Tensor stepDistanceWithCustomer;
    // public final Tensor stepDistancePickup;
    // public final Tensor stepDistanceRebalance;
    //
    // private final LinkedList<Integer> linkBuffer = new LinkedList<>();
    // private final Map<Integer, Set<Integer>> linkToObj = new HashMap<>();
    // private final Map<Integer, RoboTaxiStatus> objStatus = new HashMap<>();

    public VehicleTraceAnalyzer(int stepsMax, MatsimAmodeusDatabase db) {
        this.db = db;
        unit = db.referenceFrame.unit();
        vehicleCustomerDistance = Quantity.of(0, unit);
        vehiclePickupDistance = Quantity.of(0, unit);
        vehicleRebalancedistance = Quantity.of(0, unit);
        // ScalarUnaryOperator applyUnit = s -> Quantity.of(s, unit);
        // stepDistanceTotal = Array.zeros(stepsMax).map(applyUnit);
        // stepDistanceWithCustomer = Array.zeros(stepsMax).map(applyUnit);
        // stepDistancePickup = Array.zeros(stepsMax).map(applyUnit);
        // stepDistanceRebalance = Array.zeros(stepsMax).map(applyUnit);
    }

    public void register(int simObjIndex, VehicleContainer vc, long now) {
        // recording link and time trace
        for (int linkID : vc.linkTrace) {
            if (linkTrace.size() == 0 || linkTrace.get(linkTrace.size() - 1) != linkID) {
                linkTrace.add(linkID);
                timeTrace.add(now);
            }
        }

        // recording status at time
        statusAtTime.put(now, vc.roboTaxiStatus);

        // System.out.println(linkTrace);
        // if (!linkBuffer.isEmpty())
        // if (linkBuffer.getLast() == vc.linkTrace[0])
        // consolidate(linkBuffer.subList(0, linkBuffer.size() - 1)); // do not process last link in buffer if still relevant
        // else
        // consolidate(); // process all links in buffer
        //
        // // fill buffer
        // for (int idx : vc.linkTrace)
        // linkBuffer.addLast(idx);
        //
        // // update list of simObjects for each relevant link
        // linkToObj.entrySet().removeIf(e -> !linkBuffer.contains(e.getKey()));
        // linkBuffer.forEach(idx -> linkToObj.computeIfAbsent(idx, i -> new HashSet<>()).add(simObjIndex));
        //
        // // update relevant statuses
        // Set<Integer> allObjs = linkToObj.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        // objStatus.entrySet().removeIf(e -> !allObjs.contains(e.getKey()));
        // objStatus.put(simObjIndex, vc.roboTaxiStatus);
    }

    /* package */ void consolidate() {
        /** compute distance at every time step */
        GlobalAssert.that(linkTrace.size() == timeTrace.size());
        distanceAtTime = new TreeMap<Long, Scalar>();
        distanceAtTime.put((long) 0, Quantity.of(0, unit));
        for (int i = 1; i < linkTrace.size(); ++i) {
            if (linkTrace.get(i - 1) != linkTrace.get(i)) { // link has changed
                Scalar distanceLink = Quantity.of(db.getOsmLink(linkTrace.get(i - 1)).link.getLength(), unit);
                Scalar distanceBefore = distanceAtTime.lastEntry().getValue();
                distanceAtTime.put(timeTrace.get(i - 1), distanceBefore.add(distanceLink));
            }
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
                    vehicleCustomerDistance = vehicleCustomerDistance.add(dist);
                    break;
                case DRIVETOCUSTOMER:
                    vehiclePickupDistance = vehiclePickupDistance.add(dist);
                    break;
                case REBALANCEDRIVE:
                    vehicleRebalancedistance = vehicleRebalancedistance.add(dist);
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
    /* package */ Tensor distanceAtStep(Long time1, Long time2) {

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

// import java.util.HashMap;
// import java.util.HashSet;
// import java.util.LinkedList;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
// import java.util.stream.Collectors;
//
// import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
// import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
// import ch.ethz.idsc.amodeus.net.VehicleContainer;
// import ch.ethz.idsc.tensor.Scalar;
// import ch.ethz.idsc.tensor.Tensor;
// import ch.ethz.idsc.tensor.alg.Array;
// import ch.ethz.idsc.tensor.qty.Quantity;
// import ch.ethz.idsc.tensor.qty.Unit;
// import ch.ethz.idsc.tensor.sca.ScalarUnaryOperator;
//
/// * package */ class VehicleTraceAnalyzer {
// private final MatsimAmodeusDatabase db;
// private final Unit unit;
//
// public final Tensor stepDistanceTotal;
// public final Tensor stepDistanceWithCustomer;
// public final Tensor stepDistancePickup;
// public final Tensor stepDistanceRebalance;
//
// private final LinkedList<Integer> linkBuffer = new LinkedList<>();
// private final Map<Integer, Set<Integer>> linkToObj = new HashMap<>();
// private final Map<Integer, RoboTaxiStatus> objStatus = new HashMap<>();
//
// public VehicleTraceAnalyzer(int stepsMax, MatsimAmodeusDatabase db) {
// this.db = db;
// unit = db.referenceFrame.unit();
// ScalarUnaryOperator applyUnit = s -> Quantity.of(s, unit);
// stepDistanceTotal = Array.zeros(stepsMax).map(applyUnit);
// stepDistanceWithCustomer = Array.zeros(stepsMax).map(applyUnit);
// stepDistancePickup = Array.zeros(stepsMax).map(applyUnit);
// stepDistanceRebalance = Array.zeros(stepsMax).map(applyUnit);
// }
//
// public void register(int simObjIndex, VehicleContainer vc) {
// if (!linkBuffer.isEmpty())
// if (linkBuffer.getLast() == vc.linkTrace[0])
// consolidate(linkBuffer.subList(0, linkBuffer.size() - 1)); // do not process last link in buffer if still relevant
// else
// consolidate(); // process all links in buffer
//
// // fill buffer
// for (int idx : vc.linkTrace)
// linkBuffer.addLast(idx);
//
// // update list of simObjects for each relevant link
// linkToObj.entrySet().removeIf(e -> !linkBuffer.contains(e.getKey()));
// linkBuffer.forEach(idx -> linkToObj.computeIfAbsent(idx, i -> new HashSet<>()).add(simObjIndex));
//
// // update relevant statuses
// Set<Integer> allObjs = linkToObj.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
// objStatus.entrySet().removeIf(e -> !allObjs.contains(e.getKey()));
// objStatus.put(simObjIndex, vc.roboTaxiStatus);
// }
//
// /* package */ void consolidate() {
// consolidate(linkBuffer);
// }
//
// private void consolidate(List<Integer> toBeFlushed) {
// for (int linkIdx : toBeFlushed) {
// final double distance = db.getOsmLink(linkIdx).link.getLength();
//
// Set<Integer> relevantObj = linkToObj.get(linkIdx).stream().filter(obj -> objStatus.get(obj).isDriving()).collect(Collectors.toSet());
// final Scalar part = Quantity.of(distance / relevantObj.size(), unit);
// for (int obj : relevantObj) {
// addDistanceAt(obj, objStatus.get(obj), part);
// linkToObj.get(linkIdx).remove(obj);
// }
// }
// linkBuffer.clear();
// }
//
// private void addDistanceAt(int index, RoboTaxiStatus status, Scalar distance) {
// if (index < stepDistanceTotal.length()) {
// switch (status) {
// case DRIVEWITHCUSTOMER:
// stepDistanceWithCustomer.set(distance::add, index);
// stepDistanceTotal.set(distance::add, index); // applies to all three
// break;
// case DRIVETOCUSTOMER:
// stepDistancePickup.set(distance::add, index);
// stepDistanceTotal.set(distance::add, index); // applies to all three
// break;
// case REBALANCEDRIVE:
// stepDistanceRebalance.set(distance::add, index);
// stepDistanceTotal.set(distance::add, index); // applies to all three
// break;
// default:
// break;
// }
// }
// }
// }
//
//