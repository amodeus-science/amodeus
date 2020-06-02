/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.Optional;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.dispatcher.shared.OnMenuRequests;
import amodeus.amodeus.dispatcher.shared.SharedCourse;
import amodeus.amodeus.dispatcher.shared.SharedCourseAccess;
import amodeus.amodeus.dispatcher.shared.SharedMealType;
import amodeus.amodeus.dispatcher.shared.SharedMenu;

/** Package internal helper class to do computations for {@link} {@link RoboTaxi} which
 * are in shared use. */
/* package */ enum SharedRoboTaxiUtils {
    ;

    /** @return the {@link} Link the (shared) {@link RoboTaxi} @param roboTaxi is travelling to next. */
    public static Link getStarterLink(RoboTaxi roboTaxi) {
        Optional<SharedCourse> currentCourse = SharedCourseAccess.getStarter(roboTaxi);
        return currentCourse.map(SharedCourse::getLink).orElse(null);
    }

    /** @return true of the next {@link SharedCourse} of the {@link RoboTaxi} @param roboTaxi
     *         is of {@link SharedMealType} @param sharedMealType, otherwise @return false */
    public static boolean isNextCourseOfType(RoboTaxi roboTaxi, SharedMealType sharedMealType) {
        Optional<SharedCourse> nextcourse = SharedCourseAccess.getStarter(roboTaxi);
        return nextcourse.map(SharedCourse::getMealType).map(type -> type.equals(sharedMealType)).orElse(false);
    }

    /** @return {@link RoboTaxiStatus} of {@link RoboTaxi} @param roboTaxi computed according
     *         to its {@link SharedMenu} */
    public static RoboTaxiStatus calculateStatusFromMenu(RoboTaxi roboTaxi) {
        Optional<SharedCourse> nextCourseOptional = SharedCourseAccess.getStarter(roboTaxi);
        if (nextCourseOptional.isPresent()) {
            if (roboTaxi.getOnBoardPassengers() > 0)
                return RoboTaxiStatus.DRIVEWITHCUSTOMER;

            switch (nextCourseOptional.get().getMealType()) {
            case PICKUP:
                return RoboTaxiStatus.DRIVETOCUSTOMER;
            case REDIRECT:
                if (OnMenuRequests.getNumberMealTypes(roboTaxi.getUnmodifiableViewOfCourses(), SharedMealType.PICKUP) > 0)
                    return RoboTaxiStatus.DRIVETOCUSTOMER;
                return RoboTaxiStatus.REBALANCEDRIVE;
            default:
                throw new RuntimeException("We have a not Covered Status of the Robotaxi");
            }
        }
        return RoboTaxiStatus.STAY;
    }
}
