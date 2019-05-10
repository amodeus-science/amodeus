package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAccess;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;

/* package */ enum FinishRedirectionIfOnLastLink {
    ;

    /* package */ static final void now(RoboTaxi roboTaxi) {
        if (RoboTaxiUtils.hasNextCourse(roboTaxi)) {
            Optional<SharedCourse> currentCourse = SharedCourseAccess.getStarter(roboTaxi);
            /** search redirect courses */
            if (currentCourse.get().getMealType().equals(SharedMealType.REDIRECT)) {
                /** search if arrived at redirect destination */
                if (currentCourse.get().getLink().equals(roboTaxi.getDivertableLocation())) {
                    roboTaxi.finishRedirection();
                }
            }
        }
    }

}
