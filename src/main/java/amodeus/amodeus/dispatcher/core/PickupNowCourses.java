/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.dispatcher.shared.SharedCourse;
import amodeus.amodeus.dispatcher.shared.SharedMealType;

/* package */ class PickupNowCourses {
    ;

    public static List<SharedCourse> of(RoboTaxi roboTaxi) {
        // link of roboTaxi
        Link pickupVehicleLink = roboTaxi.getDivertableLocation();

        // find all courses which are on current link and of type PICKUP
        List<SharedCourse> pickupNowCourses = new ArrayList<>();
        for (SharedCourse course : roboTaxi.getUnmodifiableViewOfCourses()) {
            if (course.getLink().equals(pickupVehicleLink)) {
                if (course.getMealType().equals(SharedMealType.PICKUP)) {
                    pickupNowCourses.add(course);
                }
            }
        }
        // return this list
        return pickupNowCourses;
    }

}
