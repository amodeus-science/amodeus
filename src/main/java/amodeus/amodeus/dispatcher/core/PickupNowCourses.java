/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.dispatcher.shared.SharedCourse;
import amodeus.amodeus.dispatcher.shared.SharedMealType;

/* package */ class PickupNowCourses {
    ;

    public static List<SharedCourse> of(RoboTaxi roboTaxi) {
        Link pickupVehicleLink = roboTaxi.getDivertableLocation(); // link of roboTaxi
        
        if (pickupVehicleLink != roboTaxi.getCurrentDriveDestination()) {
            return Collections.emptyList();
        }

        // find all courses which are on current link and of type PICKUP
        return roboTaxi.getUnmodifiableViewOfCourses().stream() //
                .filter(course -> course.getLink().equals(pickupVehicleLink)) //
                .filter(course -> course.getMealType().equals(SharedMealType.PICKUP)) //
                .collect(Collectors.toList());
    }
}
