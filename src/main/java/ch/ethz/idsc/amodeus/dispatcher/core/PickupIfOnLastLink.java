/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAccess;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/** This function is used to execute pick-ups for shared roboTaxis, single-use/unit-capacity
 * roboTaxis are not using this class. */
/* package */ enum PickupIfOnLastLink {
    ;

    public static List<AVRequest> apply(RoboTaxi roboTaxi, double timeNow, //
            double pickupDurationPerStop, FuturePathFactory futurePathFactory) {

        // link of roboTaxi
        Link pickupVehicleLink = roboTaxi.getDivertableLocation();

        // current course on shared menu
        Optional<SharedCourse> currentCourse = SharedCourseAccess.getStarter(roboTaxi);
        GlobalAssert.that(currentCourse.isPresent());

        List<AVRequest> pickedUpRequests = new ArrayList<>();

        // check of roboTaxi is on last task
        Schedule schedule = roboTaxi.getSchedule();
        if (schedule.getCurrentTask() == Schedules.getLastTask(schedule)) {

            AVRequest avRequest = currentCourse.get().getAvRequest();
            GlobalAssert.that(currentCourse.get().getMealType().equals(SharedMealType.PICKUP));

            // roboTaxi has arrived on link of request
            if (avRequest.getFromLink().equals(pickupVehicleLink)) {

                System.err.println("pickup and assign: " + roboTaxi.getId() + "/ " + avRequest.getId());

                PickupAndAssignDirective.using(roboTaxi, Arrays.asList(avRequest), //
                        timeNow, pickupDurationPerStop, futurePathFactory);
                pickedUpRequests.add(avRequest);
            }
        }

        return pickedUpRequests;
    }

}
