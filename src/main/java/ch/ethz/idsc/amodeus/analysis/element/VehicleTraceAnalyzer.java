/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.qty.Unit;
import ch.ethz.idsc.tensor.sca.ScalarUnaryOperator;

/* package */ class VehicleTraceAnalyzer {
    private final MatsimAmodeusDatabase db;
    private final Unit unit;

    // --
    private final List<Integer> linkTrace = new ArrayList<>();

    // --
    /* package */ Scalar vehicleTotalDistance;

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
        // ScalarUnaryOperator applyUnit = s -> Quantity.of(s, unit);
        // stepDistanceTotal = Array.zeros(stepsMax).map(applyUnit);
        // stepDistanceWithCustomer = Array.zeros(stepsMax).map(applyUnit);
        // stepDistancePickup = Array.zeros(stepsMax).map(applyUnit);
        // stepDistanceRebalance = Array.zeros(stepsMax).map(applyUnit);
    }

    public void register(int simObjIndex, VehicleContainer vc, long now) {
        // System.out.println("register...");
        for (int linkID : vc.linkTrace) {
            // System.out.print(linkID + ", ");
            if (linkTrace.size() == 0 || linkTrace.get(linkTrace.size() - 1) != linkID)
                linkTrace.add(linkID);
        }

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
        double totalUnit = linkTrace.stream()//
                .mapToDouble(linkId -> db.getOsmLink(linkId).link.getLength()).sum();        
        vehicleTotalDistance = Quantity.of(totalUnit, unit);
        
        // consolidate(linkBuffer);
    }

    private void consolidate(List<Integer> toBeFlushed) {



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
    }

    private void addDistanceAt(int index, RoboTaxiStatus status, Scalar distance) {
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