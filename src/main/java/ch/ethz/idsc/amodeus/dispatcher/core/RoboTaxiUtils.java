package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;

public enum RoboTaxiUtils {
    ;
    public static boolean canPickupNewCustomer(RoboTaxi roboTaxi) {
        // TODO why does the number of customers has to be larger for a pick up?
        return roboTaxi.getCurrentNumberOfCustomersOnBoard() >= 0 && roboTaxi.getCurrentNumberOfCustomersOnBoard() < roboTaxi.getCapacity();
    }

    public static boolean checkMenuConsistency(RoboTaxi roboTaxi) {
        return SharedCourseListUtils.checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity(roboTaxi.getUnmodifiableViewOfCourses(), roboTaxi.getCapacity());
    }

    public static boolean hasNextCourse(RoboTaxi roboTaxi) {
        return SharedCourseListUtils.hasStarter(roboTaxi.getUnmodifiableViewOfCourses());
    }

    // TODO Refactor to more meaningfull name
    public static Optional<SharedCourse> getStarterCourse(RoboTaxi roboTaxi) {
        return SharedCourseListUtils.getStarterCourse(roboTaxi.getUnmodifiableViewOfCourses());
    }

    public static boolean nextCourseIsOfType(RoboTaxi roboTaxi, SharedMealType sharedMealType) {
        Optional<SharedCourse> nextcourse = getStarterCourse(roboTaxi);
        if (nextcourse.isPresent()) {
            return nextcourse.get().getMealType().equals(sharedMealType);
        }
        return false;
    }
}
