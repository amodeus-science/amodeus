/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TtlValIdent;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Join;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.io.TableBuilder;
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

    /** time series during day */
    public final Tensor time = Tensors.empty();
    public final Tensor waitTimePlotValues = Tensors.empty();

    @Override
    public void register(SimulationObject simulationObject) {

        /** build a travel history for every request */
        for (RequestContainer requestContainer : simulationObject.requests) {
            Integer requestIndex = Integer.valueOf(requestContainer.requestIndex);
            if (travelHistories.containsKey(requestIndex))
                travelHistories.get(requestIndex).register(requestContainer, simulationObject.now);
            else
                travelHistories.put(requestIndex, new TravelHistory(requestContainer, simulationObject.now));
        }
        /** analyze the distribution of wat times at every time instant */
        /** Get the TimeStep */
        time.append(RealScalar.of(simulationObject.now));
        Tensor submission = Tensor.of(simulationObject.requests.stream()//
                .filter(rc -> rc.requestStatus.unServiced())//
                .map(rc -> RealScalar.of(simulationObject.now - rc.submissionTime)));
        waitTimePlotValues.append(Join.of(StaticHelper.quantiles(submission, Quantiles.SET), //
                Tensors.vector(StaticHelper.means(submission).number().doubleValue())));

    }

    @Override
    public void consolidate() {
        for (TravelHistory travelHistory : travelHistories.values()) {
            travelTimes.appendRow(Tensors.of( //
                    RealScalar.of(travelHistory.reqIndx), travelHistory.getWaitTime(), //
                    travelHistory.getDriveTime(), travelHistory.getTotalTravelTime()));
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

        travelHistories.values().forEach(th -> th.isConsistent());

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
