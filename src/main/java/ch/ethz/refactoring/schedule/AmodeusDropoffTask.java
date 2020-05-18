package ch.ethz.refactoring.schedule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.StayTask;

import ch.ethz.matsim.av.passenger.AVRequest;

public class AmodeusDropoffTask extends StayTask {
    private final Set<AVRequest> requests = new HashSet<>();

    public AmodeusDropoffTask(double beginTime, double endTime, Link link) {
        super(AmodeusTaskType.DROPOFF, beginTime, endTime, link);
    }

    public AmodeusDropoffTask(double beginTime, double endTime, Link link, Collection<AVRequest> requests) {
        super(AmodeusTaskType.DROPOFF, beginTime, endTime, link);

        this.requests.addAll(requests);
    }

    public Set<AVRequest> getRequests() {
        return requests;
    }

    public void addRequest(AVRequest request) {
        requests.add(request);
    }
}
