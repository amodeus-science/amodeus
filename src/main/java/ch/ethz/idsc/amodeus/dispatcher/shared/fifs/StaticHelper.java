/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.shared.OnMenuRequests;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAccess;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;

/* package */ enum StaticHelper {
    ;

    public static boolean plansPickupsOrDropoffs(RoboTaxi roboTaxi) {
        if (SharedCourseAccess.hasStarter(roboTaxi.getUnmodifiableViewOfCourses())) {
            return OnMenuRequests.getNumberMealTypes(roboTaxi.getUnmodifiableViewOfCourses(), SharedMealType.DROPOFF) > 0;
        }
        return false;
    }

}
