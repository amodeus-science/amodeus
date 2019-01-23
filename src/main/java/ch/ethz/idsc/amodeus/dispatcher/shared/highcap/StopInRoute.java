package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.matsim.av.passenger.AVRequest;

// TODO might be an extension of the Shared Course instead, see the StopInRouteAsCourseExtension
/* package */ class StopInRoute {
    private final double time;
    private final Link stopLink;
    private final SharedMealType stopType;
    private final AVRequest avRequest;

    public StopInRoute(double time, Link stopLink, SharedMealType stopType, AVRequest avRequest) {
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

    public AVRequest getavRequest() {
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
