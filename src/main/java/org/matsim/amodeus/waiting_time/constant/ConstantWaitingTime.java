package org.matsim.amodeus.waiting_time.constant;

import org.matsim.amodeus.waiting_time.WaitingTime;
import org.matsim.facilities.Facility;

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
