/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.Set;

import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class MaximumWaitTimeCalculator {
    private final double maxWaitTime;
    private final double waitListTime;
    private final double extremWaitListTime;

    MaximumWaitTimeCalculator(double maxWaitTime, double waitListTime, double extremWaitListTime) {
        this.maxWaitTime = maxWaitTime;
        this.waitListTime = waitListTime;
        this.extremWaitListTime = extremWaitListTime;
    }

    double calculate(AVRequest avRequest, Set<AVRequest> waitList, Set<AVRequest> extremWaitList) {
        if (extremWaitList == null)
            return calculate(avRequest, waitList);
        return (extremWaitList.contains(avRequest)) ? extremWaitListTime : calculate(avRequest, waitList);

    }

    double calculate(AVRequest avRequest, Set<AVRequest> waitLists) {
        return waitLists.contains(avRequest) ? maxWaitTime : waitListTime;
    }
}
