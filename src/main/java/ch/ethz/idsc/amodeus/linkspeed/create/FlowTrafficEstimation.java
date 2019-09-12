/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import java.util.Objects;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.red.Norm;

public class FlowTrafficEstimation {

    /** calculates traffic delays for links {1,...,m} for trips on n routes
     * as defined in the @param flowMatrix of dimension m x n with free flow travel
     * times @param freeTimes and recorded (with congestion) travel times @param trafficTimes
     * from free flow. For the computation, a {@link TrafficDelayEstimate} @param delayCalculator is
     * required.
     * 
     * @throws Exception */
    public static FlowTrafficEstimation of(Tensor flowMatrix, Tensor freeTimes, Tensor trafficTimes, //
            TrafficDelayEstimate delayCalculator) {
        try {
            return new FlowTrafficEstimation(flowMatrix, freeTimes, trafficTimes, delayCalculator);
        } catch (Exception e) {
            System.err.println("LSQTrafficEstimation failed:");
            e.printStackTrace();
            return null;
        }
    }

    // --

    public final Tensor trafficDelays;

    private final Tensor freeTimes;
    private final Tensor trafficTimes;
    private final Tensor flowMatrix;

    private Tensor trafficTravelTimeEstimates = null;
    private Scalar error = null;

    private FlowTrafficEstimation(Tensor flowMatrix, Tensor freeTimes, Tensor trafficTimes, //
            TrafficDelayEstimate delayCalculator) throws Exception {
        this.freeTimes = freeTimes;
        this.trafficTimes = trafficTimes;
        this.flowMatrix = flowMatrix;
        Tensor deviation = trafficTimes.subtract(freeTimes);
        trafficDelays = delayCalculator.compute(flowMatrix, deviation).unmodifiable();

    }

    public Scalar getError() {
        if (Objects.isNull(trafficTravelTimeEstimates)) // very lengthy computation, to avoid repeat
            trafficTravelTimeEstimates = freeTimes.add(flowMatrix.dot(trafficDelays)).unmodifiable();
        if (Objects.isNull(error)) // very lengthy computation, to avoid repeat
            error = Norm._2.of(trafficTravelTimeEstimates.subtract(trafficTimes));
        return error;
    }

    public Tensor trafficTravelTimeEstimates() {
        if (Objects.isNull(trafficTravelTimeEstimates)) // very lengthy computation, to avoid repeat
            trafficTravelTimeEstimates = freeTimes.add(flowMatrix.dot(trafficDelays)).unmodifiable();
        return trafficTravelTimeEstimates;
    }

}
