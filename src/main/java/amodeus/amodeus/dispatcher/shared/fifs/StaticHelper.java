/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.fifs;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.shared.OnMenuRequests;
import amodeus.amodeus.dispatcher.shared.SharedCourseAccess;
import amodeus.amodeus.dispatcher.shared.SharedMealType;

/* package */ enum StaticHelper {
    ;

    public static boolean plansPickupsOrDropoffs(RoboTaxi roboTaxi) {
        return SharedCourseAccess.hasStarter(roboTaxi.getUnmodifiableViewOfCourses()) && //
                OnMenuRequests.getNumberMealTypes(roboTaxi.getUnmodifiableViewOfCourses(), SharedMealType.DROPOFF) > 0;
    }
}
