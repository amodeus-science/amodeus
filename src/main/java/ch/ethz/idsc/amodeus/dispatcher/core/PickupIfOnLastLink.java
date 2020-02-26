/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

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

    public static Optional<AVRequest> apply(RoboTaxi roboTaxi, double timeNow, double pickupDurationPerStop, //
            FuturePathFactory futurePathFactory) {
        // link of roboTaxi
        Link pickupVehicleLink = roboTaxi.getDivertableLocation();

        // current course on shared menu
        Optional<SharedCourse> currentCourse = SharedCourseAccess.getStarter(roboTaxi);
        GlobalAssert.that(currentCourse.isPresent());

        // check of roboTaxi is on last task
        Schedule schedule = roboTaxi.getSchedule();
        if (schedule.getCurrentTask() == Schedules.getLastTask(schedule)) {

            AVRequest avRequest = currentCourse.get().getAvRequest();
            GlobalAssert.that(currentCourse.get().getMealType().equals(SharedMealType.PICKUP));

            // roboTaxi has arrived on link of request
            if (avRequest.getFromLink().equals(pickupVehicleLink)) {
                pickupAndAssignDirective(roboTaxi, Arrays.asList(avRequest), //
                        timeNow, pickupDurationPerStop, futurePathFactory);
                return Optional.of(avRequest);
            }
        }
        return Optional.empty();
    }

    private static void pickupAndAssignDirective(RoboTaxi roboTaxi, List<AVRequest> commonOriginRequests, //
            double now, double pickupDurationPerStop, FuturePathFactory futurePathFactory) {

        // all requests must have same from link
        GlobalAssert.that(commonOriginRequests.stream().map(AVRequest::getFromLink).distinct().count() == 1);
        Link commonFromLink = commonOriginRequests.get(0).getFromLink();

        // ensure that roboTaxi has enough capacity
        int onBoard = (int) roboTaxi.getOnBoardPassengers();
        int pickupN = commonOriginRequests.size();
        GlobalAssert.that(onBoard + pickupN <= roboTaxi.getCapacity());

        // Update the roboTaxi menu // must be done for each request!
        for (AVRequest request : commonOriginRequests)
            roboTaxi.pickupNewCustomerOnBoard();
        roboTaxi.setCurrentDriveDestination(commonFromLink);

        // Assign Directive
        final double endPickupTime = now + pickupDurationPerStop;
        FuturePathContainer futurePathContainer = //
                futurePathFactory.createFuturePathContainer(commonFromLink, //
                        SharedRoboTaxiUtils.getStarterLink(roboTaxi), endPickupTime);
        roboTaxi.assignDirective(new SharedGeneralPickupDirective(roboTaxi, commonOriginRequests, //
                futurePathContainer, now));

        GlobalAssert.that(!roboTaxi.isDivertable());

        // ensure that pickup is not in taxi schedule, drop-off still is
        for (AVRequest avRequest2 : commonOriginRequests) {
            GlobalAssert.that(!roboTaxi.getUnmodifiableViewOfCourses().contains(SharedCourse.pickupCourse(avRequest2)));
            GlobalAssert.that(roboTaxi.getUnmodifiableViewOfCourses().contains(SharedCourse.dropoffCourse(avRequest2)));
        }

    }

}
