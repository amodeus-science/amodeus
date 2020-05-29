package org.matsim.amodeus.dvrp.schedule;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.StayTask;

public class AmodeusDropoffTask extends StayTask {
    private final Map<Id<Request>, PassengerRequest> requests = new HashMap<>();

    public AmodeusDropoffTask(double beginTime, double endTime, Link link) {
        super(AmodeusTaskType.DROPOFF, beginTime, endTime, link);
    }

    public void addRequest(PassengerRequest request) {
        requests.put(request.getId(), request);
    }

    public Map<Id<Request>, PassengerRequest> getRequests() {
        return requests;
    }
}
