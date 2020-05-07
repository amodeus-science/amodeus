package ch.ethz.matsim.av.waiting_time;

import org.matsim.facilities.Facility;

public interface WaitingTime {
    double getWaitingTime(Facility facility, double time);
}
