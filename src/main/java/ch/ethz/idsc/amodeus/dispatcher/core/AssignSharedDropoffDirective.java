/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAccess;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum AssignSharedDropoffDirective {
    ;

    public static final Optional<AVRequest> apply(RoboTaxi roboTaxi, double now, double dropoffDurationPerStop, FuturePathFactory futurePathFactory) {
        Link dropoffVehicleLink = roboTaxi.getDivertableLocation();
        // SHARED note that waiting for last staytask adds a one second staytask before
        // switching to dropoffTask
        // This excludes as well that Requests are droped off a second time and dropping off during pickup
        if (roboTaxi.getSchedule().getCurrentTask() == Schedules.getLastTask(roboTaxi.getSchedule())) {
            Optional<SharedCourse> currentCourse = SharedCourseAccess.getStarter(roboTaxi);
            GlobalAssert.that(currentCourse.isPresent());
            if (currentCourse.get().getMealType().equals(SharedMealType.DROPOFF)) {
                AVRequest avRequest = currentCourse.get().getAvRequest();
                GlobalAssert.that(roboTaxi.isWithoutDirective());
                if (avRequest.getToLink().equals(dropoffVehicleLink)) {
                    assignDropoffDirective(roboTaxi, avRequest, now, dropoffDurationPerStop, futurePathFactory);
                    return Optional.of(avRequest);
                }
            }
        }
        return Optional.empty();
    }

    private static final void assignDropoffDirective(RoboTaxi roboTaxi, AVRequest avRequest, double now, double dropoffDurationPerStop, FuturePathFactory futurePathFactory) {

        // CHECK That Dropoff Is Possible
        Optional<SharedCourse> currentCourse = SharedCourseAccess.getStarter(roboTaxi);
        GlobalAssert.that(currentCourse.isPresent());
        GlobalAssert.that(currentCourse.get().getMealType().equals(SharedMealType.DROPOFF));
        GlobalAssert.that(currentCourse.get().getCourseId().equals(avRequest.getId().toString()));
        GlobalAssert.that(currentCourse.get().getLink().equals(avRequest.getToLink()));
        GlobalAssert.that(currentCourse.get().getLink().equals(roboTaxi.getDivertableLocation()));
        final Schedule schedule = roboTaxi.getSchedule();
        GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule)); // instanceof AVDriveTask);

        // Assign Directive To roboTaxi
        final double endDropOffTime = now + dropoffDurationPerStop;
        Optional<SharedCourse> secondCourse = SharedCourseAccess.getSecond(roboTaxi.getUnmodifiableViewOfCourses());
        final Link endLink = (secondCourse.isPresent()) ? secondCourse.get().getLink() : avRequest.getToLink();
        FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer(avRequest.getToLink(), endLink, endDropOffTime);
        roboTaxi.assignDirective(new SharedGeneralDropoffDirective(roboTaxi, avRequest, futurePathContainer, now, dropoffDurationPerStop));

    }

}
