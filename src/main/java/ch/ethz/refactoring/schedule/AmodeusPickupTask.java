package ch.ethz.refactoring.schedule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.StayTask;

import ch.ethz.matsim.av.passenger.AVRequest;

public class AmodeusPickupTask extends StayTask {
    final private Set<AVRequest> requests = new HashSet<>();
    private final double earliestDepartureTime;

    public AmodeusPickupTask(double beginTime, double endTime, Link link, double earliestDepartureTime) {
        super(AmodeusTaskType.PICKUP, beginTime, endTime, link);
        this.earliestDepartureTime = earliestDepartureTime;
    }

    public AmodeusPickupTask(double beginTime, double endTime, Link link, double earliestDepartureTime, Collection<AVRequest> requests) {
        this(beginTime, endTime, link, earliestDepartureTime);

        this.requests.addAll(requests);
    }

    public void addRequest(AVRequest request) {
        requests.add(request);
    }

    public Set<AVRequest> getRequests() {
        return requests;
    }

    public double getEarliestDepartureTime() {
        return earliestDepartureTime;
    }
}
