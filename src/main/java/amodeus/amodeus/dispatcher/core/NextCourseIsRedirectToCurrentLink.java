/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.Optional;

import amodeus.amodeus.dispatcher.shared.SharedCourse;
import amodeus.amodeus.dispatcher.shared.SharedCourseAccess;
import amodeus.amodeus.dispatcher.shared.SharedMealType;

/* package */ enum NextCourseIsRedirectToCurrentLink {
    ;

    public static boolean check(RoboTaxi roboTaxi) {
        Optional<SharedCourse> redirectCourseCheck = SharedCourseAccess.getStarter(roboTaxi);
        if (!redirectCourseCheck.isPresent())
            return false;
        if (!redirectCourseCheck.get().getMealType().equals(SharedMealType.REDIRECT))
            return false;
        return redirectCourseCheck.get().getLink().equals(roboTaxi.getDivertableLocation());
    }

}
