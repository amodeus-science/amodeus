/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.dispatcher.core.LinkStatusPair;
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
import org.matsim.api.core.v01.network.Link;

/* package */ class VehicleTraceAnalyzer {
    private final VehicleTraceAnalyzerOld vehicleTraceAnalyzerOld;
    private final VehicleTraceAnalyzerNew vehicleTraceAnalyzerNew;

    // --
    /* package */ Scalar vehicleTotalDistance;
    /* package */ Scalar vehicleCustomerDist;
    /* package */ Scalar vehiclePickupDist;
    /* package */ Scalar vehicleRebalancedist;

    public VehicleTraceAnalyzer(MatsimAmodeusDatabase db) {
        vehicleTraceAnalyzerOld = new VehicleTraceAnalyzerOld(db);
        vehicleTraceAnalyzerNew = new VehicleTraceAnalyzerNew(db);
    }

    public void register(VehicleContainer vc, long now) {
        vehicleTraceAnalyzerOld.register(vc, now);
        vehicleTraceAnalyzerNew.register(vc, now);
    }

    /* package */ void consolidate() {
        // TODO distance differs in length of last link: solved
        vehicleTraceAnalyzerOld.consolidate();
        vehicleTraceAnalyzerNew.consolidate();

        vehicleTotalDistance = vehicleTraceAnalyzerNew.vehicleTotalDistance;
        vehicleCustomerDist = vehicleTraceAnalyzerNew.vehicleCustomerDist;
        vehiclePickupDist = vehicleTraceAnalyzerNew.vehiclePickupDist;
        vehicleRebalancedist = vehicleTraceAnalyzerNew.vehicleRebalancedist;

        // System.out.println("Total:\t" + vehicleTraceAnalyzerOld.vehicleTotalDistance + " =?= " + vehicleTraceAnalyzerNew.vehicleTotalDistance);
        // System.out.println("Customer:\t" + vehicleTraceAnalyzerOld.vehicleCustomerDist + " =?= " + vehicleTraceAnalyzerNew.vehicleCustomerDist);
        // System.out.println("Pickup:\t" + vehicleTraceAnalyzerOld.vehiclePickupDist + " =?= " + vehicleTraceAnalyzerNew.vehiclePickupDist);
        // System.out.println("Rebalancing:\t" + vehicleTraceAnalyzerOld.vehicleRebalancedist + " =?= " + vehicleTraceAnalyzerNew.vehicleRebalancedist);
    }

    /** @return at time step {@link Long} @param time1 encoded
     *         as {total distance, with customer,pickup,rebalance} */
    /* package */ Tensor labeledIntervalDistance(Long time1, Long time2) {
        Tensor oldDist = vehicleTraceAnalyzerOld.labeledIntervalDistance(time1, time2);
        Tensor newDist = vehicleTraceAnalyzerNew.labeledIntervalDistance(time1, time2);
        // System.out.println("old: " + oldDist);
        // System.out.println("new: " + newDist);
        return newDist;
    }
}



// TODO the linkTrace and timeTrace lists could be partially emptied during the register 
// steps to save memory, this should be done in the long-term
/* package */ class VehicleTraceAnalyzerNew {
    private static final List<RoboTaxiStatus> DRIVING_STATII = //
            Arrays.asList(RoboTaxiStatus.DRIVEWITHCUSTOMER, RoboTaxiStatus.DRIVETOCUSTOMER, RoboTaxiStatus.REBALANCEDRIVE);

    private final MatsimAmodeusDatabase db;
    private final Unit unit;

    private final NavigableMap<Long, List<LinkStatusPair>> history = new TreeMap<>();
    private final NavigableSet<Long> times = new TreeSet<>();

    private NavigableMap<Long, Tensor> distances;

    // --
    /* package */ Scalar vehicleTotalDistance;
    /* package */ Scalar vehicleCustomerDist;
    /* package */ Scalar vehiclePickupDist;
    /* package */ Scalar vehicleRebalancedist;

    public VehicleTraceAnalyzerNew(MatsimAmodeusDatabase db) {
        this.db = db;
        unit = db.referenceFrame.unit();
        vehicleCustomerDist = Quantity.of(0, unit);
        vehiclePickupDist = Quantity.of(0, unit);
        vehicleRebalancedist = Quantity.of(0, unit);
    }

    public void register(VehicleContainer vc, long now) {
        times.add(now);
        Optional<Link> lastLink = history.values().stream().flatMap(Collection::stream).reduce((v1, v2) -> v2).map(pair -> pair.link);
        // Optional<Link> lastLink = Optional.ofNullable(history.lastEntry()).map(Entry::getValue).filter(l -> !l.isEmpty()).map(l -> l.get(l.size() - 1)).map(pair -> pair.link);
        for (int i = 0; i < vc.linkTrace.length; i++) {
            Link link = db.getOsmLink(vc.linkTrace[i]).link;
            if (i > 0 || !lastLink.map(link::equals).orElse(false)) {
                RoboTaxiStatus roboTaxiStatus = vc.statii[i];
                if (roboTaxiStatus != RoboTaxiStatus.STAY) {
                    history.computeIfAbsent(now, l -> new ArrayList<>()).add(new LinkStatusPair(link, roboTaxiStatus));
                } else if (vc.linkTrace.length > i + 1 && link.getId().index() != vc.linkTrace[i + 1]) { // investigate why this status is assigned anyway
                    roboTaxiStatus = vc.statii[i + 1];
                    if (roboTaxiStatus != RoboTaxiStatus.STAY)
                        history.computeIfAbsent(now, l -> new ArrayList<>()).add(new LinkStatusPair(link, roboTaxiStatus));
                } else
                    GlobalAssert.that(history.isEmpty()); // TODO remove if proven, maybe reblace check by isDriving
            }
        }

        // System.out.println("vehicle:" + vc.vehicleIndex + ",\t" + vc.roboTaxiStatus);
        // System.out.println("\t" + Arrays.stream(vc.linkTrace).mapToObj(Integer::toString).collect(Collectors.joining(", ")));
        // System.out.println("\t" + Arrays.stream(vc.statii).map(RoboTaxiStatus::toString).collect(Collectors.joining(", ")));
    }

    // alternative
    // public void register(VehicleContainer vc, long now) {
    //     times.add(now);
    //     for (int i = 0; i < vc.linkTrace.length; i++)
    //         history.computeIfAbsent(now, t -> new ArrayList<>()).add(new LinkStatusPair(db.getOsmLink(vc.linkTrace[i]).link, vc.statii[i]));
    // }

    /* package */ void consolidate() {
        /** compute distance at every time step */
        distances = history.entrySet().stream().collect(Collectors.toMap( //
                Entry::getKey, e -> distance(e.getValue()), (v1, v2) -> { throw new RuntimeException(); }, TreeMap::new));

        // System.out.println("new: " + Accumulate.of(Tensor.of(distances.values().stream().map(t -> t.Get(0)).map(Round._6))));

        /** compute total distances */
        Tensor totalDistances = distances.values().stream().reduce(Tensor::add).orElse(emptyDistance());
        vehicleTotalDistance = totalDistances.Get(0);
        vehicleCustomerDist = totalDistances.Get(DRIVING_STATII.indexOf(RoboTaxiStatus.DRIVEWITHCUSTOMER) + 1);
        vehiclePickupDist = totalDistances.Get(DRIVING_STATII.indexOf(RoboTaxiStatus.DRIVETOCUSTOMER) + 1);
        vehicleRebalancedist = totalDistances.Get(DRIVING_STATII.indexOf(RoboTaxiStatus.REBALANCEDRIVE) + 1);
    }

    // alternative
    // /* package */ void consolidate() {
    //     /** clean history */
    //     // correct wrongly labeled stay
    //     history.values().forEach(pairs -> IntStream.rangeClosed(2, pairs.size()).map(i -> pairs.size() - i) //
    //             .filter(i -> pairs.get(i).roboTaxiStatus == RoboTaxiStatus.STAY) //
    //             .filter(i -> pairs.get(i + 1).roboTaxiStatus != RoboTaxiStatus.STAY) //
    //             .filter(i -> pairs.get(i).link !=  pairs.get(i + 1).link) //
    //             .forEach(i -> pairs.set(i, new LinkStatusPair(pairs.get(i).link, pairs.get(i + 1).roboTaxiStatus))));
    //
    //     // remove stay
    //     history.values().forEach(pairs -> pairs.removeIf(p -> p.roboTaxiStatus == RoboTaxiStatus.STAY));
    //
    //     // remove duplicate links
    //     AtomicInteger pastLink = new AtomicInteger(-1);
    //     history.values().forEach(pairs -> pairs.removeIf(p -> {
    //         int id = p.link.getId().index();
    //         return id == pastLink.getAndSet(id);
    //     }));
    //
    //     // remove empty entries
    //     history.entrySet().removeIf(e -> e.getValue().isEmpty());
    //
    //     /** compute distance at every time step */
    //     distances = history.entrySet().stream().collect(Collectors.toMap( //
    //             Entry::getKey, e -> distance(e.getValue()), (v1, v2) -> { throw new RuntimeException(); }, TreeMap::new));
    //
    //     // System.out.println("fut: " + Accumulate.of(Tensor.of(distances.values().stream().map(t -> t.Get(0)).map(Round._6))));
    //
    //     /** compute total distances */
    //     Tensor totalDistances = distances.values().stream().reduce(Tensor::add).orElse(emptyDistance());
    //     vehicleTotalDistance = totalDistances.Get(0);
    //     vehicleCustomerDist = totalDistances.Get(DRIVING_STATII.indexOf(RoboTaxiStatus.DRIVEWITHCUSTOMER) + 1);
    //     vehiclePickupDist = totalDistances.Get(DRIVING_STATII.indexOf(RoboTaxiStatus.DRIVETOCUSTOMER) + 1);
    //     vehicleRebalancedist = totalDistances.Get(DRIVING_STATII.indexOf(RoboTaxiStatus.REBALANCEDRIVE) + 1);
    // }

    private Tensor distance(Collection<LinkStatusPair> pairs) {
        Tensor distance = emptyDistance();
        for (LinkStatusPair pair : pairs) {
            Scalar length = Quantity.of(db.getOsmLink(pair.link).getLength(), unit);
            distance.set(length::add, 0);
            if (DRIVING_STATII.contains(pair.roboTaxiStatus))
                distance.set(length::add, DRIVING_STATII.indexOf(pair.roboTaxiStatus) + 1);
        }
        return distance;
    }

    private Tensor emptyDistance() {
        return Array.fill(() -> Quantity.of(0, unit), 4);
    }

    /** @param time1 interval start (exclusive)
     * @param time2 interval end (inclusive)
     * @return distance covered in the interval as {total distance, with customer, pickup, rebalance} */
    /* package */ Tensor labeledIntervalDistance(long time1, long time2) {
        return labeledDistanceUntil(time2).subtract(labeledDistanceUntil(time1));
    }

    /** @param time (inclusive)
     * @return distance covered until time as {total distance, with customer, pickup, rebalance} */
    private Tensor labeledDistanceUntil(long time) {
        Tensor totalDistances = distances.headMap(time, true).values().stream().reduce(Tensor::add).orElse(emptyDistance());

        // linear interpolation including missing data points
        if (!distances.containsKey(time)) {
            Long keyHigher = distances.ceilingKey(time);
            if (Objects.nonNull(keyHigher)) {
                long keyLower = Optional.ofNullable(distances.lowerKey(time)).orElse(0L);
                Tensor dist = distances.get(keyHigher).multiply(RationalScalar.of(time - keyLower, keyHigher - keyLower));
                totalDistances = totalDistances.add(dist);
            }
        }
        /*
        // linear interpolation with missing data point = no distance
        if (!distances.containsKey(time)) {
            Optional<Long> optional = Optional.ofNullable(times.ceiling(time));
            if (optional.isPresent()) {
                long keyLower = Optional.ofNullable(times.lower(time)).orElse(0L);
                long keyHigher = optional.get();
                Tensor dist = distances.getOrDefault(keyHigher, emptyDistance()) //
                        .multiply(RationalScalar.of(time - keyLower, keyHigher - keyLower));
                totalDistances = totalDistances.add(dist);
            }
        }
        */
        return totalDistances;
    }
}




/* package */ class VehicleTraceAnalyzerOld {
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
    /* package */ NavigableMap<Long, RoboTaxiStatus> statusAtTime = new TreeMap<>();

    public VehicleTraceAnalyzerOld(MatsimAmodeusDatabase db) {
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
        distanceAtTime = new TreeMap<>();
        distanceAtTime.put((long) 0, Quantity.of(0, unit));
        for (int i = 1; i < linkTrace.size(); ++i)  // last link is forgotten
            if (linkTrace.get(i - 1) != linkTrace.get(i)) { // link has changed
                Scalar distanceLink = Quantity.of(db.getOsmLink(linkTrace.get(i - 1)).link.getLength(), unit);
                Scalar distanceBefore = distanceAtTime.lastEntry().getValue();
                distanceAtTime.put(timeTrace.get(i - 1), distanceBefore.add(distanceLink));
            }

        // System.out.println("old: " + Tensor.of(distanceAtTime.values().stream().skip(1).map(Round._6)));
        // System.out.println(db.getOsmLink(linkTrace.get(linkTrace.size() - 1)).getLength());

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
        // Entry<Long, Scalar> entryLower = distanceAtTime.lowerEntry(timeLow); // why lower and not floor? prevents exact queries
        Entry<Long, Scalar> entryLower = distanceAtTime.floorEntry(timeLow);
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