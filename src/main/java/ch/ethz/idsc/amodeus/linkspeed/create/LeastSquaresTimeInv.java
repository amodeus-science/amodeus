/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer;
import ch.ethz.idsc.amodeus.linkspeed.create.TaxiLinkSpeedEstimator;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.routing.CachedNetworkPropertyComputation;
import ch.ethz.idsc.amodeus.routing.EasyMinTimePathCalculator;
import ch.ethz.idsc.amodeus.routing.TimeResolvedPathProperty;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTripCheck;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.geo.ClosestLinkSelect;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.qty.Quantity;


public class LeastSquaresTimeInv implements TaxiLinkSpeedEstimator {

    private final CachedNetworkPropertyComputation<NavigableMap<Double, Link>> travelTimeCalculator;
    private final ClosestLinkSelect closestLinkSelect;
    private final LinkSpeedDataContainer lsData;
    public final static Scalar dayDt = Quantity.of(3600, "s");

    public LeastSquaresTimeInv(Collection<TaxiTrip> records, Network network, //
            AmodeusTimeConvert timeConvert, MatsimAmodeusDatabase db, QuadTree<Link> qt, //
            LocalDate simulationDate, TrafficDelayEstimate delayCalculator) throws Exception {

        /** ensure {@link TaxiTrip}s contain all required information */
        GlobalAssert.that(records.stream().filter(TaxiTripCheck::isOfMinimalScope).count() == records.size());

        /** compute a path for every record, scale path such that end time
         * is as in the {@link TaxiTrip}, then break into parts as time
         * steps of recording */
        travelTimeCalculator = new CachedNetworkPropertyComputation<>//
        (EasyMinTimePathCalculator.prepPathCalculator(//
                network, new FastAStarLandmarksFactory()), 180000.0, TimeResolvedPathProperty.INSTANCE);
        closestLinkSelect = new ClosestLinkSelect(db, qt);

        Collection<PathHandlerTimeInv> pathes = new ArrayList<>();
        records.stream().forEach(tt -> //
        pathes.add(new PathHandlerTimeInv(tt, travelTimeCalculator, timeConvert, closestLinkSelect, simulationDate)));
        System.out.println("Totally found " + pathes.size() + " for least squares speed computation.");

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
        int numVar = travelledLinks.size();// indexDtbase.size();

        Tensor flowMatrix = Array.zeros(numEq, numVar);
        Tensor freeflowDuration = Array.zeros(numEq, 1);
        Tensor trafficDuration = Array.zeros(numEq, 1);

        int k = 0;
        for (PathHandlerTimeInv ph : pathes) {
            for (Link link : ph.travelledLinks) {
                int index = localIndexLink2.get(link);
                flowMatrix.set(RealScalar.ONE, k, index);
            }
            freeflowDuration.set(RealScalar.of(ph.freeflowDuation.number()), k, 0);
            trafficDuration.set(RealScalar.of(ph.duration.number()), k, 0);
            ++k;
        }

        /** least squares traffic estimation */
        LSQTrafficEstimation estimation = LSQTrafficEstimation.of(flowMatrix, freeflowDuration, trafficDuration//
                , delayCalculator);
        Tensor estimateTravelTimes = estimation.trafficTravelTimeEstimates;
        System.out.println("Error of estimation per road: " + estimation.error.divide(RealScalar.of(numVar)));
        Export.object(new File("/home/clruch/Desktop/estimation"), estimation);

        /** take solution xij and assign to LinkSpeedDataContainer
         * (necessary to transfer to deviations from freeflow speed) */
        this.lsData = new LinkSpeedDataContainer();
        int numNegSpeed = 0;
        for (int i = 0; i < estimateTravelTimes.length(); ++i) {
            Link link = localIndexLink.get(i);
            Objects.requireNonNull(link);
            GlobalAssert.that(network.getLinks().values().contains(link));
            int linkID = Integer.parseInt(link.getId().toString());
            for (int bin = 0; bin * dayDt.number().intValue() < 108000; ++bin) {
                int time = RealScalar.of(bin).multiply(dayDt).number().intValue();
                GlobalAssert.that(time >= 0);
                double speed = link.getLength() / estimateTravelTimes.Get(i, 0).number().doubleValue();
                double freeSpeed = link.getFreespeed();
                double wLSQ = 0.625;
                double weightedSpeed = wLSQ * speed + (1 - wLSQ) * freeSpeed;
                if (speed >= 0) {
                    lsData.addData(linkID, time, weightedSpeed);
                } else {
                    ++numNegSpeed;
                }
            }
        }
        System.out.println("Number of negative speeds ommitted: " + numNegSpeed);

        // TODO print links for which the result is higher than the freeflow speed
        /** Apply moving average filter to modify every link not solved in the least squares step */
        ProximityNeighborKernel filterKernel = new ProximityNeighborKernel(network, (Quantity) Quantity.of(1000, "m"));
        LinkSpeedDataInterpolation interpolation = new LinkSpeedDataInterpolation(network, filterKernel, lsData, db);
    }

    @Override
    public LinkSpeedDataContainer getLsData() {
        return lsData;
    }
}
