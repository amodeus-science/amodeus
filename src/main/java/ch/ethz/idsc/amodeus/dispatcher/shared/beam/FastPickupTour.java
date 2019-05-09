package ch.ethz.idsc.amodeus.dispatcher.shared.beam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAdd;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseRemove;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ enum FastPickupTour {
    ;

    public static List<SharedCourse> fastPickupTour(List<SharedCourse> unmodifiableSharedMenu, Coord startCoord) {
        List<SharedCourse> originalSharedCourses = new ArrayList<>(unmodifiableSharedMenu);

        GlobalAssert.that(StaticHelper.checkAllPickupsFirst(originalSharedCourses));
        int originalSize = originalSharedCourses.size();
        Collection<SharedCourse> sharedCourses = originalSharedCourses.stream()//
                .filter(sc -> sc.getMealType().equals(SharedMealType.PICKUP)).collect(Collectors.toList());

        for (SharedCourse sharedCourse : sharedCourses) {
            SharedCourseRemove.specific(originalSharedCourses, sharedCourse);
        }

        int currentIndex = 0;
        Coord nextCoord = startCoord;
        while (!sharedCourses.isEmpty()) {
            SharedCourse closestCourse = StaticHelper.getClosestCourse(sharedCourses, nextCoord);
            SharedCourseAdd.atIndexList(originalSharedCourses, closestCourse, currentIndex);
            currentIndex++;
            nextCoord = closestCourse.getLink().getCoord();
            sharedCourses.remove(closestCourse);
        }

        GlobalAssert.that(originalSize == originalSharedCourses.size());
        return originalSharedCourses;
    }

}
