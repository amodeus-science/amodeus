/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.shared.OnMenuRequests;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAccess;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;

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
        if (nextcourse.isPresent())
            return nextcourse.get().getMealType().equals(sharedMealType);
        return false;
    }

    /** @return {@link RoboTaxiStatus} of {@link RoboTaxi} @param roboTaxi computed according
     *         to its {@link SharedMenu} */
    public static RoboTaxiStatus calculateStatusFromMenu(RoboTaxi roboTaxi) {
        Optional<SharedCourse> nextCourseOptional = SharedCourseAccess.getStarter(roboTaxi);
        if (nextCourseOptional.isPresent()) {
            if (roboTaxi.getMenuOnBoardCustomers() > 0)
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
