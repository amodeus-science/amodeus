/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.Optional;

import amodeus.amodeus.dispatcher.shared.SharedCourse;
import amodeus.amodeus.dispatcher.shared.SharedCourseAccess;
import amodeus.amodeus.dispatcher.shared.SharedMealType;

/* package */ enum FinishRedirectionIfOnLastLink {
    ;

    /* package */ static void now(RoboTaxi roboTaxi) {
        if (SharedCourseAccess.hasStarter(roboTaxi)) {
            Optional<SharedCourse> currentCourse = SharedCourseAccess.getStarter(roboTaxi);
            /** search redirect courses */
            if (currentCourse.get().getMealType().equals(SharedMealType.REDIRECT))
                /** search if arrived at redirect destination */
                if (currentCourse.get().getLink().equals(roboTaxi.getDivertableLocation()))
                    roboTaxi.finishRedirection();
        }
    }

}
