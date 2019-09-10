/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import java.io.Serializable;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.red.Norm;

public class LSQTrafficEstimation implements Serializable {

    /** calculates traffic delays for links {1,...,n} for trips on k routes
     * as defined in the @param flowMatrix of dimension k x n with free flow trave
     * times @param freeTimes and recorded (with congestion) travel times @param trafficTimes
     * from free flow. For the computation, a {@link TrafficDelayEstimate} @param delayCalculator is
     * required.
     * 
     * @throws Exception */
    public static LSQTrafficEstimation of(Tensor flowMatrix, Tensor freeTimes, Tensor trafficTimes, //
            TrafficDelayEstimate delayCalculator) {
        try {
            return new LSQTrafficEstimation(flowMatrix, freeTimes, trafficTimes, delayCalculator);
        } catch (Exception e) {
            System.err.println("LSQTrafficEstimation failed:");
            e.printStackTrace();
            return null;
        }
    }

    // --

    public final Tensor trafficDelays;
    public final Tensor trafficTravelTimeEstimates;
    public final Tensor error;

    private LSQTrafficEstimation(Tensor flowMatrix, Tensor freeTimes, Tensor trafficTimes, //
            TrafficDelayEstimate delayCalculator) throws Exception {
        Tensor deviation = trafficTimes.subtract(freeTimes);
        trafficDelays = delayCalculator.compute(flowMatrix, deviation).unmodifiable();
        trafficTravelTimeEstimates = freeTimes.add(flowMatrix.dot(trafficDelays)).unmodifiable();
        error = Norm._2.of(trafficTravelTimeEstimates.subtract(trafficTimes)).unmodifiable();
    }

}
