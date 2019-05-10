/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.shared.OnboardRequests;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAccess;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseUtil;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;


// TODO refactor
public enum RoboTaxiUtils {
    ;



    public static boolean plansPickupsOrDropoffs(RoboTaxi roboTaxi) {
        if (hasNextCourse(roboTaxi)) {
            return OnboardRequests.getNumberMealTypes(roboTaxi.getUnmodifiableViewOfCourses(), SharedMealType.DROPOFF) > 0;
        }
        return false;
    }

    public static boolean hasNextCourse(RoboTaxi roboTaxi) {
        return SharedCourseAccess.hasStarter(roboTaxi.getUnmodifiableViewOfCourses());
    }

    public static boolean hasSecondCourse(RoboTaxi roboTaxi) {
        return SharedCourseAccess.hasSecondCourse(roboTaxi.getUnmodifiableViewOfCourses());
    }



    public static Link getStarterLink(RoboTaxi roboTaxi) {
        Optional<SharedCourse> currentCourse = SharedCourseAccess.getStarter(roboTaxi);
        GlobalAssert.that(currentCourse.isPresent());
        return currentCourse.get().getLink();
    }

    public static boolean nextCourseIsOfType(RoboTaxi roboTaxi, SharedMealType sharedMealType) {
        Optional<SharedCourse> nextcourse = SharedCourseAccess.getStarter(roboTaxi);
        if (nextcourse.isPresent()) {
            return nextcourse.get().getMealType().equals(sharedMealType);
        }
        return false;
    }

    public static Set<AVRequest> getRequestsInMenu(RoboTaxi roboTaxi) {
        return SharedCourseUtil.getUniqueAVRequests(roboTaxi.getUnmodifiableViewOfCourses());
    }

    /* package */ static RoboTaxiStatus calculateStatusFromMenu(RoboTaxi roboTaxi) {
        Optional<SharedCourse> nextCourseOptional = SharedCourseAccess.getStarter(roboTaxi);
        if (nextCourseOptional.isPresent()) {
            if (OnboardRequests.getNumberOnBoardRequests(roboTaxi) > 0) {
                return RoboTaxiStatus.DRIVEWITHCUSTOMER;
            } else //
            if (nextCourseOptional.get().getMealType().equals(SharedMealType.PICKUP)) {
                return RoboTaxiStatus.DRIVETOCUSTOMER;
            } else //
            if (nextCourseOptional.get().getMealType().equals(SharedMealType.REDIRECT)) {
                if (OnboardRequests.getNumberMealTypes(roboTaxi.getUnmodifiableViewOfCourses(), SharedMealType.PICKUP) > 0) {
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
