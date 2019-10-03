/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.linkspeed.LinkIndex;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.taxitrip.ShortestDurationCalculator;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTripCheck;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.Dimensions;
import ch.ethz.idsc.tensor.qty.Quantity;

public class FlowTimeInvLinkSpeed implements TaxiLinkSpeedEstimator {

    private final LinkSpeedDataContainer lsData;
    public final static Scalar dayDt = Quantity.of(3600, "s");

    public FlowTimeInvLinkSpeed(Collection<TaxiTrip> records, Network network, //
            MatsimAmodeusDatabase db, TrafficDelayEstimate delayCalculator) throws Exception {

        /** ensure {@link TaxiTrip}s contain all required information */
        GlobalAssert.that(records.stream().filter(TaxiTripCheck::isOfMinimalScope).count() == records.size());

        /** compute a path for every record, scale path such that end time
         * is as in the {@link TaxiTrip}, then break into parts as time
         * steps of recording */
        ShortestDurationCalculator calc = new ShortestDurationCalculator(network, db);
        Collection<PathHandlerTimeInv> pathes = new ArrayList<>();
        records.stream().forEach(tt -> //
        pathes.add(new PathHandlerTimeInv(tt, calc)));
        System.out.println("Totally found " + pathes.size() + " paths for flow link speed computation.");

        /** remove paths which trip duration > free flow duration */
        Collection<PathHandlerTimeInv> remove = new ArrayList<>();
        pathes.stream().filter(p -> !p.isValid()).forEach(p -> remove.add(p));
        pathes.removeAll(remove);

        /** record traveled links */
        Set<Link> travelledLinks = new HashSet<>();
        pathes.forEach(p -> {
            p.travelledLinks.forEach(l -> {
                travelledLinks.add(l);
            });
        });

        HashMap<Integer, Link> localIndexLink = new HashMap<>();
        HashMap<Link, Integer> localIndexLink2 = new HashMap<>();
        int j = 0;
        for (Link link : travelledLinks) {
            localIndexLink.put(j, link);
            localIndexLink2.put(link, j);
            ++j;
        }
        GlobalAssert.that(travelledLinks.size() == localIndexLink.size());

        /** setup matrices A, b, solve pseudo-inverse */
        int numEq = records.size();
        int numVar = travelledLinks.size();

        Tensor flowMatrix = Array.zeros(numEq, numVar);
        Tensor freeflowTripDuration = Array.zeros(numEq, 1);
        Tensor trafficTripDuration = Array.zeros(numEq, 1);

        int k = 0;
        for (PathHandlerTimeInv ph : pathes) {
            for (Link link : ph.travelledLinks) {
                int index = localIndexLink2.get(link);
                flowMatrix.set(RealScalar.ONE, k, index);
            }
            freeflowTripDuration.set(RealScalar.of(ph.freeflowDuation.number()), k, 0);
            trafficTripDuration.set(RealScalar.of(ph.duration.number()), k, 0);
            ++k;
        }

        /** flow based traffic estimation */
        FlowTrafficEstimation estimation = //
                FlowTrafficEstimation.of(flowMatrix, freeflowTripDuration, trafficTripDuration, delayCalculator);

        Tensor estimateTravelTimeLinkDelays = estimation.trafficDelays;// trafficTravelTimeEstimates();

        int nwLinks = network.getLinks().size();
        System.out.println("Number of network links:       " + nwLinks);
        List<Integer> dims = Dimensions.of(flowMatrix);
        System.out.println("Number of covered links:       " + dims.get(1) + "(" + dims.get(1) / ((double) nwLinks) + ")");
        System.out.println("Trips used for calculation:    " + dims.get(0));

        boolean verbose = false;
        if (verbose) { // enabling these prints will result in very substantial workload...
            System.out.println("trafficDelays:              " + Dimensions.of(estimation.trafficDelays));
            System.out.println("trafficTravelTimeEstimates: " + Dimensions.of(estimation.trafficTravelTimeEstimates()));
            System.out.println("error:                      " + Dimensions.of(estimation.getError()));
        }

        /** take solution xij and assign to LinkSpeedDataContainer
         * (necessary to transfer to deviations from freeflow speed) */
        this.lsData = new LinkSpeedDataContainer();
        int numNegSpeed = 0;

        int numReduction = 0;
        int numIncrease = 0;
        int numSame = 0;

        for (int i = 0; i < estimateTravelTimeLinkDelays.length(); ++i) {
            Link link = localIndexLink.get(i);
            Objects.requireNonNull(link);
            GlobalAssert.that(network.getLinks().values().contains(link));
            String linkID = LinkIndex.fromLink(link);

            /** calculate the link speed */
            double length = link.getLength();
            double freeSpeed = link.getFreespeed();
            double freeTravelTime = length / freeSpeed;
            GlobalAssert.that(freeSpeed > 0);
            GlobalAssert.that(length > 0);

            double congestionDelay = estimateTravelTimeLinkDelays.Get(i, 0).number().doubleValue();
            if (congestionDelay < 0)
                System.err.println("congestionDelay: " + congestionDelay);
            GlobalAssert.that(congestionDelay >= 0);

            double trafficTravelTime = freeTravelTime + congestionDelay;
            double speed = length / trafficTravelTime;
            if (trafficTravelTime < freeTravelTime) {
                System.err.println("traffic: " + trafficTravelTime);
                System.err.println("free:    " + freeTravelTime);
            }

            if (speed == freeSpeed)
                ++numSame;
            if (speed < freeSpeed)
                ++numReduction;
            if (speed - freeSpeed > 0.1) {
                ++numIncrease;
                System.out.println("speed:             " + speed);
                System.out.println("freeSpeed:         " + freeSpeed);
                System.out.println("length:            " + length);
                System.out.println("trafficTravelTime: " + trafficTravelTime);
                System.out.println("===");
            }

            /** add for all times */
            for (int bin = 0; bin * dayDt.number().intValue() < 108000; ++bin) {
                int time = RealScalar.of(bin).multiply(dayDt).number().intValue();
                GlobalAssert.that(time >= 0);
                if (trafficTravelTime >= 0) {
                    GlobalAssert.that(speed - link.getFreespeed() <= 0.01);
                    lsData.addData(linkID, time, speed);
                } else {
                    ++numNegSpeed;
                }
            }
        }

        System.out.println("Number of negative speeds ommitted: " + numNegSpeed);
        System.out.println("numReduction: " + numReduction);
        System.out.println("numSame: " + numSame);
        System.out.println("numIncrease: " + numIncrease);
        // System.exit(1);

        /** Apply moving average filter to modify every link not solved in the previous step */
        ProximityNeighborKernel filterKernel = new ProximityNeighborKernel(network, (Quantity) Quantity.of(2000, "m"));
        LinkSpeedDataInterpolation interpolation = new LinkSpeedDataInterpolation(network, filterKernel, lsData, db);
    }

    @Override
    public LinkSpeedDataContainer getLsData() {
        return lsData;
    }
}
