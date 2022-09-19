package org.matsim.amodeus.dvrp.schedule;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.DefaultStayTask;

public class AmodeusStopTask extends DefaultStayTask {
    public enum StopType {
        Pickup, Dropoff
    }

    private final Map<Id<Request>, PassengerRequest> pickupRequests = new HashMap<>();
    private final Map<Id<Request>, PassengerRequest> dropoffRequests = new HashMap<>();

    private final StopType stopType;

    public AmodeusStopTask(double beginTime, double endTime, Link link, StopType stopType) {
        super(AmodeusTaskTypes.STOP, beginTime, endTime, link);
        this.stopType = stopType;
    }

    public void addPickupRequest(PassengerRequest request) {
        pickupRequests.put(request.getId(), request);
    }

    public void addDropoffRequest(PassengerRequest request) {
        dropoffRequests.put(request.getId(), request);
    }

    public Map<Id<Request>, PassengerRequest> getPickupRequests() {
        return pickupRequests;
    }

    public Map<Id<Request>, PassengerRequest> getDropoffRequests() {
        return dropoffRequests;
    }

    public StopType getStopType() {
        return stopType;
    }
}
