package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;

import ch.ethz.idsc.amodeus.dispatcher.shared.OnboardRequests;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum PickupIfOnLastLink {
    ;

    public static Optional<AVRequest> apply(RoboTaxi roboTaxi, double timeNow, double pickupDurationPerStop, FuturePathFactory futurePathFactory) {
        // Same for dropoffs
        Link pickupVehicleLink = roboTaxi.getDivertableLocation();
        // SHARED note that waiting for last staytask adds a one second staytask before
        // switching to pickuptask
        if (roboTaxi.getSchedule().getCurrentTask() == Schedules.getLastTask(roboTaxi.getSchedule())) {
            Optional<SharedCourse> currentCourse = RoboTaxiUtils.getStarterCourse(roboTaxi);
            GlobalAssert.that(currentCourse.isPresent());
            AVRequest avRequest = currentCourse.get().getAvRequest();
            GlobalAssert.that(currentCourse.get().getMealType().equals(SharedMealType.PICKUP));

            if (avRequest.getFromLink().equals(pickupVehicleLink)) {
                pickupAndAssignDirective(roboTaxi, avRequest, timeNow, pickupDurationPerStop, futurePathFactory);
                return Optional.of(avRequest);
            }
        }
        return Optional.empty();
    }

    private static final void pickupAndAssignDirective(RoboTaxi roboTaxi, AVRequest avRequest, double now, double pickupDurationPerStop, FuturePathFactory futurePathFactory) {
        GlobalAssert.that(OnboardRequests.canPickupNewCustomer(roboTaxi));
        Optional<SharedCourse> currentCourse = RoboTaxiUtils.getStarterCourse(roboTaxi);
        GlobalAssert.that(currentCourse.isPresent());
        GlobalAssert.that(currentCourse.get().getMealType().equals(SharedMealType.PICKUP));
        GlobalAssert.that(currentCourse.get().getCourseId().equals(avRequest.getId().toString()));
        GlobalAssert.that(currentCourse.get().getLink().equals(avRequest.getFromLink()));
        GlobalAssert.that(currentCourse.get().getLink().equals(roboTaxi.getDivertableLocation()));
        final Schedule schedule = roboTaxi.getSchedule();
        GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule)); // check that current task is last task in schedule

        // Update the Robo Taxi
        roboTaxi.pickupNewCustomerOnBoard(); // comments by luc: inside this function, the first course in menu is removed.
        roboTaxi.setCurrentDriveDestination(currentCourse.get().getLink());

        // Assign Directive
        final double endPickupTime = now + pickupDurationPerStop;
        FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer(avRequest.getFromLink(), RoboTaxiUtils.getStarterLink(roboTaxi), endPickupTime);
        roboTaxi.assignDirective(new SharedGeneralPickupDirective(roboTaxi, avRequest, futurePathContainer, now));

        GlobalAssert.that(!roboTaxi.isDivertable());

        // After Function Checks
        GlobalAssert.that(!roboTaxi.getUnmodifiableViewOfCourses().contains(SharedCourse.pickupCourse(avRequest)));

        GlobalAssert.that(roboTaxi.getUnmodifiableViewOfCourses().contains(SharedCourse.dropoffCourse(avRequest)));
    }

}
