/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.fifs;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

/* package */ class SharedRoutePoint {
    /** additional fields to the Shared Course */
    private final double arrivalTime;
    private final double stopDuration;
    private final Directive directive;

    public SharedRoutePoint(Directive directive, double arrivalTime, double stopDuration) {
        this.arrivalTime = arrivalTime;
        this.stopDuration = stopDuration;
        this.directive = directive;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getEndTime() {
        return arrivalTime + stopDuration;
    }

    public Directive getDirective() {
        return directive;
    }

    public boolean isStop() {
        return directive instanceof StopDirective;
    }

    public boolean isPickup() {
        if (isStop()) {
            return ((StopDirective) directive).isPickup();
        }

        return false;
    }

    public boolean isDropoff() {
        if (isStop()) {
            return !((StopDirective) directive).isPickup();
        }

        return false;
    }

    public PassengerRequest getRequest() {
        if (isStop()) {
            return ((StopDirective) directive).getRequest();
        }

        throw new IllegalStateException();
    }
}
