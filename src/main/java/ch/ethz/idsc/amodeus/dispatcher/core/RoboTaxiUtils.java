package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;
import java.util.Set;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.matsim.av.passenger.AVRequest;

public enum RoboTaxiUtils {
    ;
    public static boolean canPickupNewCustomer(RoboTaxi roboTaxi) {
        // TODO why does the number of customers has to be larger for a pick up?
        int onBoard = RoboTaxiUtils.getNumberOnBoardRequests(roboTaxi); 
        return onBoard >= 0 && onBoard < roboTaxi.getCapacity();
    }

    public static boolean checkMenuConsistency(RoboTaxi roboTaxi) {
        return SharedCourseListUtils.checkMenuConsistency(roboTaxi.getUnmodifiableViewOfCourses(), roboTaxi.getCapacity());
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
    
    public static Set<AVRequest> getAvRequestsOnBoard(RoboTaxi roboTaxi) {
        return SharedCourseListUtils.getOnBoardRequests(roboTaxi.getUnmodifiableViewOfCourses());
    }
    public static Set<String> getIdsOfAvRequestsOnBoard(RoboTaxi roboTaxi) {
        return SharedCourseListUtils.getOnBoardRequestIds(roboTaxi.getUnmodifiableViewOfCourses());
    }
    
    public static int getNumberOnBoardRequests(RoboTaxi roboTaxi) {
        return (int) SharedCourseListUtils.getNumberCustomersOnBoard(roboTaxi.getUnmodifiableViewOfCourses());
    }
    
}
