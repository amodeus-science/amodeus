/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.fifs;

import amodeus.amodeus.dispatcher.core.schedule.directives.DefaultStopDirective;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

/* package */ class SharedRoutePoint extends DefaultStopDirective {
    /** additional fields to the Shared Course */
    private final double arrivalTime;
    private final double stopDuration;

    public SharedRoutePoint(StopDirective directive, double arrivalTime, double stopDuration) {
        super(directive.getRequest(), directive.isPickup(), directive.isModifiable());
        this.arrivalTime = arrivalTime;
        this.stopDuration = stopDuration;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getEndTime() {
        return arrivalTime + stopDuration;
    }
}
