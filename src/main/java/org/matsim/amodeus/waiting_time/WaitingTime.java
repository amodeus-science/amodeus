package org.matsim.amodeus.waiting_time;

import org.matsim.facilities.Facility;

public interface WaitingTime {
    double getWaitingTime(Facility facility, double time);
}
