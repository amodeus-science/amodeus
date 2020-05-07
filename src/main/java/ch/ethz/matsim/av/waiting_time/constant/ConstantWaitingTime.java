package ch.ethz.matsim.av.waiting_time.constant;

import org.matsim.facilities.Facility;

import ch.ethz.matsim.av.waiting_time.WaitingTime;

public class ConstantWaitingTime implements WaitingTime {
    private final double defaultWaitingTime;

    public ConstantWaitingTime(double defaultWaitingTime) {
        this.defaultWaitingTime = defaultWaitingTime;
    }

    @Override
    public double getWaitingTime(Facility facility, double time) {
        return defaultWaitingTime;
    }
}
