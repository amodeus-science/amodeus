/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.highcap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;

// TODO @sebhoerl might be an extension of the Shared Course instead, see the StopInRouteAsCourseExtension
/* package */ class StopInRoute {
    private final double time;
    private final Link stopLink;
    private final boolean isPickup;
    private final PassengerRequest avRequest;

    public StopInRoute(double time, Link stopLink, boolean isPickup, PassengerRequest avRequest) {
        this.time = time;
        this.stopLink = stopLink;
        this.isPickup = isPickup;
        this.avRequest = avRequest;
    }

    public Link getStopLink() {
        return stopLink;
    }

    public boolean isPickup() {
        return isPickup;
    }

    public double getTime() {
        return time;
    }

    public PassengerRequest getavRequest() {
        return avRequest;
    }

    public Directive getSharedCourse() {
        if (isPickup)
            return Directive.pickup(avRequest);

        if (!isPickup)
            return Directive.dropoff(avRequest);
        
        return null;

        // throw new RuntimeException("my problem is ...");
    }
}
