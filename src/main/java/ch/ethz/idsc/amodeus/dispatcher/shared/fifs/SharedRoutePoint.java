/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;

/* package */ class SharedRoutePoint extends SharedCourse {
    /** additional fields to the Shared Course */
    private final double arrivalTime;
    private final double stopDuration;

    SharedRoutePoint(SharedCourse sharedCourse, double arrivalTime, double stopDuration) {
        super(sharedCourse.getAvRequest(), sharedCourse.getLink(), sharedCourse.getCourseId(), sharedCourse.getMealType());
        this.arrivalTime = arrivalTime;
        this.stopDuration = stopDuration;
    }

    double getArrivalTime() {
        return arrivalTime;
    }

    double getEndTime() {
        return arrivalTime + stopDuration;
    }
}
