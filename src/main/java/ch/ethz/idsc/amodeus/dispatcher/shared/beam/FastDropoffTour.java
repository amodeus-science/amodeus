package ch.ethz.idsc.amodeus.dispatcher.shared.beam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAdd;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseRemove;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ class FastDropoffTour {
    ;

    public static List<SharedCourse> fastDropoffTour(List<SharedCourse> unmodifiableSharedMenu) {
        List<SharedCourse> sharedMenu = new ArrayList<>(unmodifiableSharedMenu);
        GlobalAssert.that(StaticHelper.checkAllPickupsFirst(sharedMenu));
        GlobalAssert.that(!sharedMenu.stream().anyMatch(sc -> sc.getMealType().equals(SharedMealType.REDIRECT)));

        Coord lastPickupCoord = getLastPickup(sharedMenu).getLink().getCoord();
        Set<SharedCourse> set = new HashSet<>();
        for (SharedCourse sharedCourse : sharedMenu) {
            if (sharedCourse.getMealType().equals(SharedMealType.DROPOFF)) {
                set.add(sharedCourse);
            }
        }

        for (SharedCourse sharedCourse : set) {
            SharedCourseRemove.removeAVCourse(sharedMenu, sharedCourse);
        }

        int numIter = set.size();
        for (int i = 0; i < numIter; i++) {
            SharedCourse sharedCourse = StaticHelper.getClosestCourse(set, lastPickupCoord);
            SharedCourseAdd.asDessert(sharedMenu, sharedCourse);
            set.remove(sharedCourse);
        }
        GlobalAssert.that(set.isEmpty());
        return sharedMenu;
    }

    private static SharedCourse getLastPickup(List<SharedCourse> sharedMenu) {
        SharedCourse lastCourse = null;
        for (SharedCourse sharedCourse : sharedMenu) {
            if (sharedCourse.getMealType().equals(SharedMealType.PICKUP)) {
                lastCourse = sharedCourse;
            }
        }
        return lastCourse;
    }

}
