/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.highcap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.shared.SharedCourse;
import amodeus.amodeus.dispatcher.shared.SharedMealType;

// TODO @sebhoerl might be an extension of the Shared Course instead, see the StopInRouteAsCourseExtension
/* package */ class StopInRoute {
    private final double time;
    private final Link stopLink;
    private final SharedMealType stopType;
    private final PassengerRequest avRequest;

    public StopInRoute(double time, Link stopLink, SharedMealType stopType, PassengerRequest avRequest) {
        this.time = time;
        this.stopLink = stopLink;
        this.stopType = stopType;
        this.avRequest = avRequest;
    }

    public Link getStopLink() {
        return stopLink;
    }

    public SharedMealType getStopType() {
        return stopType;
    }

    public double getTime() {
        return time;
    }

    public PassengerRequest getavRequest() {
        return avRequest;
    }

    public SharedCourse getSharedCourse() {
        if (stopType.equals(SharedMealType.PICKUP))
            return SharedCourse.pickupCourse(avRequest);

        if (stopType.equals(SharedMealType.DROPOFF))
            return SharedCourse.dropoffCourse(avRequest);

        throw new RuntimeException("my problem is ...");
    }
}
