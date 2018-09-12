/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TtlValIdent;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Join;
import ch.ethz.idsc.tensor.red.Max;

public class WaitingTimesElement implements AnalysisElement, TotalValueAppender {

    public final Tensor time = Tensors.empty();
    /** map contains the final waiting time for every request */
    public final Map<Integer, Double> requestWaitTimes = new HashMap<>();

    public final Tensor waitTimePlotValues = Tensors.empty();

    private Tensor uniqueSubmissionsWaitTimes;
    public double maximumWaitTime;
    public Tensor ttlWaitTQuantile;
    public Scalar totalWaitTimeMean;

    // total Values for TotalValuesFile
    private final Map<TotalValueIdentifier, String> totalValues = new HashMap<>();

    @Override
    public void register(SimulationObject simulationObject) {

        /** Get the TimeStep */
        time.append(RealScalar.of(simulationObject.now));

        Tensor submission = Tensor.of(waitingRequests(simulationObject.requests).map(rc -> RealScalar.of(simulationObject.now - rc.submissionTime)));
        waitingRequests(simulationObject.requests).forEach(rc -> requestWaitTimes.put(rc.requestIndex, simulationObject.now - rc.submissionTime));

        waitTimePlotValues.append(Join.of(StaticHelper.quantiles(submission, Quantiles.SET), Tensors.vector(StaticHelper.means(submission).number().doubleValue())));
    }

    private static Stream<RequestContainer> waitingRequests(List<RequestContainer> allRequests) {
        return allRequests.stream().filter(rc -> rc.requestStatus.unServiced());
    }

    @Override
    public void consolidate() {
        uniqueSubmissionsWaitTimes = Tensor.of(requestWaitTimes.values().stream().map(RealScalar::of));
        maximumWaitTime = uniqueSubmissionsWaitTimes.flatten(-1).reduce(Max::of).get().Get().number().doubleValue();
        ttlWaitTQuantile = StaticHelper.quantiles(uniqueSubmissionsWaitTimes, Quantiles.SET);
        totalWaitTimeMean = StaticHelper.means(uniqueSubmissionsWaitTimes);
    }

    @Override
    public Map<TotalValueIdentifier, String> getTotalValues() {
        totalValues.put(TtlValIdent.WAITTMAX, String.valueOf(maximumWaitTime));
        totalValues.put(TtlValIdent.WAITTMEA, String.valueOf(totalWaitTimeMean));
        totalValues.put(TtlValIdent.WAITTQU1, String.valueOf(ttlWaitTQuantile.Get(0).number().doubleValue()));
        totalValues.put(TtlValIdent.WAITTQU2, String.valueOf(ttlWaitTQuantile.Get(1).number().doubleValue()));
        totalValues.put(TtlValIdent.WAITTQU3, String.valueOf(ttlWaitTQuantile.Get(2).number().doubleValue()));
        return totalValues;
    }

}