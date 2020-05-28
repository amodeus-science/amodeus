package org.matsim.amodeus.dvrp.schedule;

import java.util.HashMap;
import java.util.Map;

import org.matsim.amodeus.dvrp.request.AVRequest;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.schedule.StayTask;

public class AmodeusDropoffTask extends StayTask {
    private final Map<Id<Request>, AVRequest> requests = new HashMap<>();

    public AmodeusDropoffTask(double beginTime, double endTime, Link link) {
        super(AmodeusTaskType.DROPOFF, beginTime, endTime, link);
    }

    public void addRequest(AVRequest request) {
        requests.put(request.getId(), request);
    }

    public Map<Id<Request>, AVRequest> getRequests() {
        return requests;
    }
}
