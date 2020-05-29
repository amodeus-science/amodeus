package org.matsim.amodeus.components.dispatcher.multi_od_heuristic.aggregation;

import java.util.Collection;
import java.util.LinkedList;

import org.matsim.amodeus.components.dispatcher.multi_od_heuristic.TravelTimeEstimator;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

public class AggregatedRequest {
    final private PassengerRequest master;
    final Collection<PassengerRequest> slaves = new LinkedList<>();

    final long occupancyThreshold;
    final double distanceThreshold;

    private DvrpVehicle vehicle = null;

    final private TravelTimeEstimator estimator;

    public AggregatedRequest(PassengerRequest master, TravelTimeEstimator estimator, long occupancyThreshold) {
        this.master = master;
        this.estimator = estimator;
        this.occupancyThreshold = occupancyThreshold;

        distanceThreshold = estimator.getTravelTimeThreshold();
    }

    public PassengerRequest getMasterRequest() {
        return master;
    }

    public void addSlaveRequest(PassengerRequest slave) {
        slaves.add(slave);
    }

    public Collection<PassengerRequest> getSlaveRequests() {
        return slaves;
    }

    public Double accept(PassengerRequest candidate) {
        if (slaves.size() >= occupancyThreshold - 1) {
            return null;
        }

        double distance1 = estimator.estimateTravelTime(candidate.getToLink(), master.getToLink(), master.getEarliestStartTime());

        if (distance1 > distanceThreshold) {
            return null;
        }

        double distance2 = estimator.estimateTravelTime(candidate.getFromLink(), master.getFromLink(), master.getEarliestStartTime());

        if (distance2 > distanceThreshold) {
            return null;
        }

        return distance1 + distance2;
    }

    public void setVehicle(DvrpVehicle vehicle) {
        this.vehicle = vehicle;
    }

    public DvrpVehicle getVehicle() {
        return vehicle;
    }
}
