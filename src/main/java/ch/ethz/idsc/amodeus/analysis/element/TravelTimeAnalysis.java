/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.router.DistanceAsTravelDisutility;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;

import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TtlValIdent;
import ch.ethz.idsc.amodeus.dispatcher.core.RStatusHelper;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Join;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.io.TableBuilder;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.red.Max;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Quantile;

public class TravelTimeAnalysis implements AnalysisElement, TotalValueAppender {

    private final Map<Integer, TravelHistory> travelHistories = new TreeMap<>();
    public final TableBuilder travelTimes = new TableBuilder();
    public final TableBuilder requstStmps = new TableBuilder();

    /** aggregate values {quantile1, quantile2, quantile3, min} */
    private Tensor waitTAgg;
    private Tensor drveAgg;
    private Tensor totJTAgg;
    private Scalar tLast = Quantity.of(0, SI.SECOND);

    /** time series during day */
    public final Tensor time = Tensors.empty();
    public final Tensor waitTimePlotValues = Tensors.empty();
    public final Tensor waitingCustomers = Tensors.empty();

    /** standard Time Calculation */
    private final LeastCostPathCalculator lcpc;
    private final MatsimStaticDatabase db = MatsimStaticDatabase.INSTANCE;
    private Tensor xDTAgg;

    public TravelTimeAnalysis(Network network) {
        // TODO might be moved to a Place where we have the acutal lcpc available.
        FastAStarLandmarksFactory factory = new FastAStarLandmarksFactory();
        TravelDisutility disutility = new DistanceAsTravelDisutility();
        TravelTime travelTime = new FreeSpeedTravelTime();
        lcpc = factory.createPathCalculator(network, disutility, travelTime);
    }

    @Override
    public void register(SimulationObject simulationObject) {

        /** build a travel history for every request */
        for (RequestContainer requestContainer : simulationObject.requests) {
            Integer requestIndex = Integer.valueOf(requestContainer.requestIndex);
            if (travelHistories.containsKey(requestIndex))
                travelHistories.get(requestIndex).register(requestContainer, Quantity.of(simulationObject.now, SI.SECOND));
            else
                travelHistories.put(requestIndex, new TravelHistory(requestContainer, simulationObject.now));
        }
        /** analyze the distribution of wait times at every time instant
         * and the number of waiting customers */
        time.append(RealScalar.of(simulationObject.now));
        Tensor submission = Tensor.of(simulationObject.requests.stream()//
                .filter(rc -> RStatusHelper.unserviced(rc.requestStatus))//
                .map(rc -> RealScalar.of(simulationObject.now - rc.submissionTime)));
        waitTimePlotValues.append(Join.of(StaticHelper.quantiles(submission, Quantiles.SET), //
                Tensors.vector(StaticHelper.means(submission).number().doubleValue())));
        waitingCustomers.append(RationalScalar.of(simulationObject.requests.stream()//
                .filter(rc -> RStatusHelper.unserviced(rc.requestStatus)).count(), 1));//

        /** maximum time */
        tLast = Quantity.of(simulationObject.now, SI.SECOND);

    }

    @Override
    public void consolidate() {
        /** calculate standard dropoff time. */
        travelHistories.values().forEach(th -> th.calculateStandardDrpOffTime(lcpc, db));
        travelHistories.values().forEach(th -> th.fillNotFinishedData(tLast));
        /** finish filling of travel Histories */
        for (TravelHistory travelHistory : travelHistories.values()) {
            travelTimes.appendRow(Tensors.of( //
                    RealScalar.of(travelHistory.reqIndx), travelHistory.getWaitTime(), //
                    travelHistory.getDriveTime(), travelHistory.getTotalTravelTime(), //
                    travelHistory.getExtraDriveTime()));
            requstStmps.appendRow(Tensors.of( //
                    RealScalar.of(travelHistory.reqIndx), travelHistory.submsnTime, //
                    travelHistory.getAssignmentTime(), travelHistory.getWaitEndTime(), //
                    travelHistory.getDropOffTime()));
        }

        /** aggregate information {quantile1, quantile2, quantile3, mean, maximum} */
        waitTAgg = Tensors.of(StaticHelper.quantiles(getWaitTimes(), Quantiles.SET), //
                Mean.of(getWaitTimes()), //
                getWaitTimes().flatten(-1).reduce(Max::of).get().Get());
        drveAgg = Tensors.of(StaticHelper.quantiles(getDriveTimes(), Quantiles.SET), //
                Mean.of(getDriveTimes()), //
                getDriveTimes().flatten(-1).reduce(Max::of).get().Get());
        totJTAgg = Tensors.of(StaticHelper.quantiles(getTotalJourneyTimes(), Quantiles.SET), //
                Mean.of(getTotalJourneyTimes()), //
                getTotalJourneyTimes().flatten(-1).reduce(Max::of).get().Get());
        xDTAgg = Tensors.of(StaticHelper.quantiles(getExtraDriveTimes(), Quantiles.SET), //
                Mean.of(getExtraDriveTimes()), //
                getExtraDriveTimes().flatten(-1).reduce(Max::of).get().Get());
    }

    /** @return {@link Tensor} containing all recorded wait times of the simulation */
    public Tensor getWaitTimes() {
        return Transpose.of(travelTimes.toTable()).get(1);
    }

    /** @return {@link Tensor} containing all recorded drive times of the simulation */
    public Tensor getDriveTimes() {
        return Transpose.of(travelTimes.toTable()).get(2);
    }

    /** @return {@link Tensor} containing all recorded total journey times of the simulation */
    public Tensor getTotalJourneyTimes() {
        return Transpose.of(travelTimes.toTable()).get(3);
    }

    /** @return {@link Tensor} containing all recorded extra drive times of the simulation */
    public Tensor getExtraDriveTimes() {
        return Transpose.of(travelTimes.toTable()).get(4);
    }

    /** @return {@link Tensor} containing
     *         {{wait time quantile 1, wait time quantile 2, wait time quantile 3},wait time mean,wait time maximum}
     *         chosen quantiles defined in {@link Quantiles} */
    public Tensor getWaitAggrgte() {
        return waitTAgg;
    }

    /** @return {@link Tensor} containing
     *         {{drive time quantile 1, drive time quantile 2, drive time quantile 3},drive time mean,drive time maximum}
     *         chosen quantiles defined in {@link Quantiles} */
    public Tensor getDrveAggrgte() {
        return drveAgg;
    }

    /** @return {@link Tensor} containing {{total travel time quantile 1, total travel time quantile 2,
     *         total travel time quantile 3},total travel time mean, total travel time maximum}
     *         chosen quantiles defined in {@link Quantiles} */
    public Tensor getTotJAggrgte() {
        return totJTAgg;
    }

    /** @return {@link Tensor} containing {{extra drive time quantile 1, extra drive time quantile 2,
     *         extra drive time quantile 3},extra drive time mean, extra drive time maximum}
     *         chosen quantiles defined in {@link Quantiles} */
    public Tensor getXDTAggrgte() {
        return xDTAgg;
    }

    @Override
    public Map<TotalValueIdentifier, String> getTotalValues() {
        Map<TotalValueIdentifier, String> totalValues = new HashMap<>();
        totalValues.put(TtlValIdent.AVERAGEJOURNEYTIMEROBOTAXI, String.valueOf(Mean.of(getTotalJourneyTimes()).Get().number().doubleValue()));

        double meanWaitTime = Mean.of(getWaitTimes()).get().Get().number().doubleValue();
        totalValues.put(TtlValIdent.WAITTMEA, String.valueOf(meanWaitTime));

        Tensor quantils = Quantile.of(getWaitTimes(), Quantiles.SET);
        totalValues.put(TtlValIdent.WAITTQU1, String.valueOf(quantils.get(0).Get().number().doubleValue()));
        totalValues.put(TtlValIdent.WAITTQU2, String.valueOf(quantils.get(1).Get().number().doubleValue()));
        totalValues.put(TtlValIdent.WAITTQU3, String.valueOf(quantils.get(2).Get().number().doubleValue()));

        double meanDriveTime = Mean.of(getDriveTimes()).get().Get().number().doubleValue();
        totalValues.put(TtlValIdent.MEANDRIVETIME, String.valueOf(meanDriveTime));

        return totalValues;
    }
}
