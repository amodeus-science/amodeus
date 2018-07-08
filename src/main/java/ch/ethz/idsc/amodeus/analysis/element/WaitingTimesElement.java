/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifiersAmodeus;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Join;
import ch.ethz.idsc.tensor.red.Max;

public class WaitingTimesElement implements AnalysisElement, TotalValueAppender {

    public static final double QUANTILE1 = 0.1;
    public static final double QUANTILE2 = 0.5;
    public static final double QUANTILE3 = 0.95;
    public static final Tensor PARAM = Tensors.vector(QUANTILE1, QUANTILE2, QUANTILE3);
    public static final String[] WAITTIMES_LABELS = new String[] { "10% quantile", "50% quantile", "95% quantile", "Mean" };

    public final Tensor time = Tensors.empty();
    public final Map<Integer, Double> requestWaitTimes = new HashMap<>();

    public final Tensor waitTimePlotValues = Tensors.empty();

    private Tensor uniqueSubmissionsWaitTimes;
    public double maximumWaitTime;
    public Tensor totalWaitTimeQuantile;
    public Scalar totalWaitTimeMean;

    // total Values for TotalValuesFile
    private final Map<TotalValueIdentifier, String> totalValues = new HashMap<>();

    @Override
    public void register(SimulationObject simulationObject) {

        /** Get the TimeStep */
        time.append(RealScalar.of(simulationObject.now));

        Tensor submission = Tensor.of(waitingRequests(simulationObject.requests).map(rc -> RealScalar.of(simulationObject.now - rc.submissionTime)));
        waitingRequests(simulationObject.requests).forEach(rc -> requestWaitTimes.put(rc.requestIndex, simulationObject.now - rc.submissionTime));

        waitTimePlotValues.append(Join.of(StaticHelper.quantiles(submission, PARAM), Tensors.vector(StaticHelper.means(submission).number().doubleValue())));
    }

    private static Stream<RequestContainer> waitingRequests(List<RequestContainer> allRequests) {
        return allRequests.stream().filter(rc -> rc.requestStatus.unServiced());
    }

    @Override
    public void consolidate() {
        uniqueSubmissionsWaitTimes = Tensor.of(requestWaitTimes.values().stream().map(RealScalar::of));
        maximumWaitTime = uniqueSubmissionsWaitTimes.flatten(-1).reduce(Max::of).get().Get().number().doubleValue();
        totalWaitTimeQuantile = StaticHelper.quantiles(uniqueSubmissionsWaitTimes, PARAM);
        totalWaitTimeMean = StaticHelper.means(uniqueSubmissionsWaitTimes);
    }

    @Override
    public Map<TotalValueIdentifier, String> getTotalValues() {
        totalValues.put(TotalValueIdentifiersAmodeus.WAITTIMEMAX, String.valueOf(maximumWaitTime));
        totalValues.put(TotalValueIdentifiersAmodeus.MEANWAITINGTIME, String.valueOf(totalWaitTimeMean));
        totalValues.put(TotalValueIdentifiersAmodeus.WAITTIMEQUANTILE1, String.valueOf(totalWaitTimeQuantile.Get(0).number().doubleValue()));
        totalValues.put(TotalValueIdentifiersAmodeus.WAITTIMEQUANTILE2, String.valueOf(totalWaitTimeQuantile.Get(1).number().doubleValue()));
        totalValues.put(TotalValueIdentifiersAmodeus.WAITTIMEQUANTILE3, String.valueOf(totalWaitTimeQuantile.Get(2).number().doubleValue()));
        return totalValues;
    }

}