package org.matsim.amodeus.dvrp.request;

import org.matsim.amodeus.routing.AmodeusRoute;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

public class AmodeusRequest implements PassengerRequest {
    private final Id<Request> id;
    private final double submissionTime;

    private final Link pickupLink;
    private final Link dropoffLink;
    private final Id<Person> passengerId;

    private final String mode;
    private final AmodeusRoute route;

    private final double maximumWaitTime;

    public AmodeusRequest(Id<Request> id, Id<Person> passengerId, Link pickupLink, Link dropoffLink, double submissionTime, String mode, AmodeusRoute route,
            double maximumWaitTime) {
        this.id = id;
        this.passengerId = passengerId;
        this.pickupLink = pickupLink;
        this.dropoffLink = dropoffLink;
        this.submissionTime = submissionTime;
        this.mode = mode;
        this.route = route;
        this.maximumWaitTime = maximumWaitTime;
    }

    @Override
    public Link getFromLink() {
        return pickupLink;
    }

    @Override
    public Link getToLink() {
        return dropoffLink;
    }

    @Override
    public Id<Person> getPassengerId() {
        return passengerId;
    }

    @Override
    public double getSubmissionTime() {
        return submissionTime;
    }

    @Override
    public Id<Request> getId() {
        return id;
    }

    @Override
    public double getEarliestStartTime() {
        return 0.0;
    }

    @Override
    public String getMode() {
        return mode;
    }

    public AmodeusRoute getRoute() {
        return route;
    }

    public double getMaximumWaitTime() {
        return maximumWaitTime;
    }

    @Override
    public double getLatestStartTime() {
        return submissionTime + maximumWaitTime;
    }
}
