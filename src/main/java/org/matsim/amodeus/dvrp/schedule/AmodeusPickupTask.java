package org.matsim.amodeus.dvrp.schedule;

import java.util.HashMap;
import java.util.Map;

import org.matsim.amodeus.dvrp.request.AVRequest;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.schedule.StayTask;

public class AmodeusPickupTask extends StayTask {
    private final Map<Id<Request>, AVRequest> requests = new HashMap<>();
    private final double earliestDepartureTime;

    public AmodeusPickupTask(double beginTime, double endTime, Link link, double earliestDepartureTime) {
        super(AmodeusTaskType.PICKUP, beginTime, endTime, link);
        this.earliestDepartureTime = earliestDepartureTime;
    }

    public void addRequest(AVRequest request) {
        requests.put(request.getId(), request);
    }

    public Map<Id<Request>, AVRequest> getRequests() {
        return requests;
    }

    public double getEarliestDepartureTime() {
        return earliestDepartureTime;
    }
}
