/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.Objects;
import java.util.Set;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

/* package */ class MaximumWaitTimeCalculator {
    private final double maxWaitTime;
    private final double waitListTime;
    private final double extremeWaitListTime;

    public MaximumWaitTimeCalculator(double maxWaitTime, double waitListTime, double extremWaitListTime) {
        this.maxWaitTime = maxWaitTime;
        this.waitListTime = waitListTime;
        this.extremeWaitListTime = extremWaitListTime;
    }

    public double calculate(PassengerRequest avRequest, Set<PassengerRequest> waitList, Set<PassengerRequest> extremeWaitList) {
        if (Objects.isNull(extremeWaitList))
            return calculate(avRequest, waitList);
        return extremeWaitList.contains(avRequest) ? extremeWaitListTime : calculate(avRequest, waitList);
    }

    public double calculate(PassengerRequest avRequest, Set<PassengerRequest> waitLists) {
        return waitLists.contains(avRequest) ? maxWaitTime : waitListTime;
    }
}
