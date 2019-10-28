/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.beam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAdd;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ enum FastDropoffTour {
    ;

    public static List<SharedCourse> fastDropoffTour(List<SharedCourse> unmodifiableSharedMenu) {
        List<SharedCourse> sharedCourses = new ArrayList<>(unmodifiableSharedMenu);
        GlobalAssert.that(StaticHelper.checkAllPickupsFirst(sharedCourses));
        GlobalAssert.that(sharedCourses.stream().noneMatch(sc -> sc.getMealType().equals(SharedMealType.REDIRECT)));

        Coord lastPickupCoord = getLastPickup(sharedCourses).getLink().getCoord();
        Set<SharedCourse> set = sharedCourses.stream().filter(course -> course.getMealType().equals(SharedMealType.DROPOFF)).collect(Collectors.toSet());

        sharedCourses.removeAll(set);

        int numIter = set.size();
        for (int i = 0; i < numIter; i++) {
            SharedCourse sharedCourse = StaticHelper.getClosestCourse(set, lastPickupCoord);
            SharedCourseAdd.asDessertList(sharedCourses, sharedCourse);
            set.remove(sharedCourse);
        }
        GlobalAssert.that(set.isEmpty());
        return sharedCourses;
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
