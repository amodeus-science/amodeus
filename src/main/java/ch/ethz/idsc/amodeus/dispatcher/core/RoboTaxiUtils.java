/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

public enum RoboTaxiUtils {
    ;
    public static boolean canPickupNewCustomer(RoboTaxi roboTaxi) {
        int onBoard = RoboTaxiUtils.getNumberOnBoardRequests(roboTaxi);
        GlobalAssert.that(onBoard >= 0);
        return onBoard < roboTaxi.getCapacity();
    }

    public static boolean checkMenuConsistency(RoboTaxi roboTaxi) {
        return SharedCourseListUtils.checkMenuConsistency(roboTaxi.getUnmodifiableViewOfCourses(), roboTaxi.getCapacity());
    }

    public static boolean plansPickupsOrDropoffs(RoboTaxi roboTaxi) {
        if (hasNextCourse(roboTaxi)) {
            return SharedCourseListUtils.getNumberDropoffs(roboTaxi.getUnmodifiableViewOfCourses()) > 0;
        }
        return false;
    }

    public static boolean hasNextCourse(RoboTaxi roboTaxi) {
        return SharedCourseListUtils.hasStarter(roboTaxi.getUnmodifiableViewOfCourses());
    }

    public static boolean hasSecondCourse(RoboTaxi roboTaxi) {
        return SharedCourseListUtils.hasSecondCourse(roboTaxi.getUnmodifiableViewOfCourses());
    }

    public static Optional<SharedCourse> getStarterCourse(RoboTaxi roboTaxi) {
        return SharedCourseListUtils.getStarterCourse(roboTaxi.getUnmodifiableViewOfCourses());
    }

    public static Optional<SharedCourse> getSecondCourse(RoboTaxi roboTaxi) {
        return SharedCourseListUtils.getSecondCourse(roboTaxi.getUnmodifiableViewOfCourses());
    }

    public static Link getStarterLink(RoboTaxi roboTaxi) {
        Optional<SharedCourse> currentCourse = RoboTaxiUtils.getStarterCourse(roboTaxi);
        GlobalAssert.that(currentCourse.isPresent());
        return currentCourse.get().getLink();
    }

    public static boolean nextCourseIsOfType(RoboTaxi roboTaxi, SharedMealType sharedMealType) {
        Optional<SharedCourse> nextcourse = getStarterCourse(roboTaxi);
        if (nextcourse.isPresent()) {
            return nextcourse.get().getMealType().equals(sharedMealType);
        }
        return false;
    }

    public static Set<AVRequest> getRequestsInMenu(RoboTaxi roboTaxi) {
        return SharedCourseListUtils.getUniqueAVRequests(roboTaxi.getUnmodifiableViewOfCourses());
    }

    public static Set<AVRequest> getAvRequestsOnBoard(RoboTaxi roboTaxi) {
        return SharedCourseListUtils.getOnBoardRequests(roboTaxi.getUnmodifiableViewOfCourses());
    }

    public static int getNumberOnBoardRequests(RoboTaxi roboTaxi) {
        return (int) SharedCourseListUtils.getNumberCustomersOnBoard(roboTaxi.getUnmodifiableViewOfCourses());
    }

    /* package */ static RoboTaxiStatus calculateStatusFromMenu(RoboTaxi roboTaxi) {
        Optional<SharedCourse> nextCourseOptional = getStarterCourse(roboTaxi);
        if (nextCourseOptional.isPresent()) {
            if (getNumberOnBoardRequests(roboTaxi) > 0) {
                return RoboTaxiStatus.DRIVEWITHCUSTOMER;
            } else //
            if (nextCourseOptional.get().getMealType().equals(SharedMealType.PICKUP)) {
                return RoboTaxiStatus.DRIVETOCUSTOMER;
            } else //
            if (nextCourseOptional.get().getMealType().equals(SharedMealType.REDIRECT)) {
                if (SharedCourseListUtils.getNumberPickups(roboTaxi.getUnmodifiableViewOfCourses()) > 0) {
                    return RoboTaxiStatus.DRIVETOCUSTOMER;
                }
                return RoboTaxiStatus.REBALANCEDRIVE;
            } else //
            if (nextCourseOptional.get().getMealType().equals(SharedMealType.PARKING)) {
                if (SharedCourseListUtils.getNumberPickups(roboTaxi.getUnmodifiableViewOfCourses()) > 0) {
                    return RoboTaxiStatus.DRIVETOCUSTOMER;
                }
                return RoboTaxiStatus.PARKING;
            } else {
                System.out.println("We have a not Covered Status of the Robotaxi");
                GlobalAssert.that(false);
            }
        }
        return RoboTaxiStatus.STAY;
    }

}
