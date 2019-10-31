/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.Objects;
import java.util.Set;

import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class MaximumWaitTimeCalculator {
    private final double maxWaitTime;
    private final double waitListTime;
    private final double extremeWaitListTime;

    public MaximumWaitTimeCalculator(double maxWaitTime, double waitListTime, double extremWaitListTime) {
        this.maxWaitTime = maxWaitTime;
        this.waitListTime = waitListTime;
        this.extremeWaitListTime = extremWaitListTime;
    }

    public double calculate(AVRequest avRequest, Set<AVRequest> waitList, Set<AVRequest> extremeWaitList) {
        if (Objects.isNull(extremeWaitList))
            return calculate(avRequest, waitList);
        return extremeWaitList.contains(avRequest) ? extremeWaitListTime : calculate(avRequest, waitList);
    }

    public double calculate(AVRequest avRequest, Set<AVRequest> waitLists) {
        return waitLists.contains(avRequest) ? maxWaitTime : waitListTime;
    }
}
