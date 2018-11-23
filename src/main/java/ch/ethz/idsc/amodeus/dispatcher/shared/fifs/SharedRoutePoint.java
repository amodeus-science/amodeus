/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class SharedRoutePoint extends SharedCourse {
    /** adtiononal fields to the Shared Course */
    private final double arrivalTime;
    private final double stopDuration;

    /* package */ SharedRoutePoint(SharedCourse sharedCourse, double arrivalTime, double stopDuration) {
        super(sharedCourse.getAvRequest(), sharedCourse.getLink(), sharedCourse.getCourseId(), sharedCourse.getMealType());
        this.arrivalTime = arrivalTime;
        this.stopDuration = stopDuration;
    }

    /* package */ SharedRoutePoint(AVRequest avRequest, Link link, String avRequestId, SharedMealType sharedMealType, double arrivalTime, double stopDuration) {
        super(avRequest, link, avRequestId, sharedMealType);
        this.arrivalTime = arrivalTime;
        this.stopDuration = stopDuration;
    }

    /* package */ double getArrivalTime() {
        return arrivalTime;
    }

    /* package */ double getStopDuration() {
        return stopDuration;
    }

    /* package */ double getEndTime() {
        return arrivalTime + stopDuration;
    }
}
