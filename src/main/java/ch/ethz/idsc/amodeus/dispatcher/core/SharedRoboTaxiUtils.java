/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.shared.OnMenuRequests;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAccess;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** Package internal helper class to do computations for {@link} {@link RoboTaxi} which
 * are in shared use. */
public enum SharedRoboTaxiUtils {
    ;

    /** @return the {@link} Link the (shared) {@link RoboTaxi} @param roboTaxi is travelling to next. */
    /* package */ static Link getStarterLink(RoboTaxi roboTaxi) {
        Optional<SharedCourse> currentCourse = SharedCourseAccess.getStarter(roboTaxi);
        if (currentCourse.isPresent())
            return currentCourse.get().getLink();
        else
            return null;
    }

    /** @return true of the next {@link SharedCourse} of the {@link RoboTaxi} @param roboTaxi
     *         is of {@link SharedMealType} @param sharedMealType, otherwise @return false */
    /* package */ static boolean isNextCourseOfType(RoboTaxi roboTaxi, SharedMealType sharedMealType) {
        Optional<SharedCourse> nextcourse = SharedCourseAccess.getStarter(roboTaxi);
        if (nextcourse.isPresent())
            return nextcourse.get().getMealType().equals(sharedMealType);
        return false;
    }

    /** @return {@link RoboTaxiStatus} of {@link RoboTaxi} @param roboTaxi computed according
     *         to its {@link SharedMenu} */
    /* package */ static RoboTaxiStatus calculateStatusFromMenu(RoboTaxi roboTaxi) {
        Optional<SharedCourse> nextCourseOptional = SharedCourseAccess.getStarter(roboTaxi);
        if (nextCourseOptional.isPresent()) {
            if (OnMenuRequests.getOnBoardCustomers(roboTaxi) > 0) {
                return RoboTaxiStatus.DRIVEWITHCUSTOMER;
            } else //
            if (nextCourseOptional.get().getMealType().equals(SharedMealType.PICKUP)) {
                return RoboTaxiStatus.DRIVETOCUSTOMER;
            } else //
            if (nextCourseOptional.get().getMealType().equals(SharedMealType.REDIRECT)) {
                if (OnMenuRequests.getNumberMealTypes(roboTaxi.getUnmodifiableViewOfCourses(), SharedMealType.PICKUP) > 0) {
                    return RoboTaxiStatus.DRIVETOCUSTOMER;
                }
                return RoboTaxiStatus.REBALANCEDRIVE;
            } else {
                System.out.println("We have a not Covered Status of the Robotaxi");
                GlobalAssert.that(false);
            }
        }
        return RoboTaxiStatus.STAY;
    }

}
