/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;

import amodeus.amodeus.dispatcher.shared.SharedCourse;

/** This function is used to execute pick-ups for shared roboTaxis, single-use/unit-capacity
 * roboTaxis are not using this class. */
/* package */ enum PickupIfOnLastLink {
    ;

    public static List<PassengerRequest> apply(RoboTaxi roboTaxi, double timeNow, //
            double pickupDurationPerStop, FuturePathFactory futurePathFactory) {

        List<SharedCourse> courses = PickupNowCourses.of(roboTaxi);
        List<PassengerRequest> pickupNowRequests = courses.stream().map(c -> c.getAvRequest())//
                .collect(Collectors.toList());

        // check of roboTaxi is on last task
        Schedule schedule = roboTaxi.getSchedule();
        if (schedule.getCurrentTask() == Schedules.getLastTask(schedule)) {

            PickupAndAssignDirective.using(roboTaxi, pickupNowRequests, //
                    timeNow, pickupDurationPerStop, futurePathFactory);

            return pickupNowRequests;
        }
        return new ArrayList<>();
    }
}
