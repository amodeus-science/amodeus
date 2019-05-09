package ch.ethz.idsc.amodeus.dispatcher.shared.beam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ enum FastPickupTour {
    ;

    public static List<SharedCourse> fastPickupTour(List<SharedCourse> unmodifiableSharedMenu, Coord startCoord) {
        List<SharedCourse> sharedMenu = new ArrayList<>(unmodifiableSharedMenu);

        GlobalAssert.that(StaticHelper.checkAllPickupsFirst(sharedMenu));
        int originalSize = sharedMenu.size();
        Collection<SharedCourse> sharedCourses = sharedMenu.stream().filter(sc -> sc.getMealType().equals(SharedMealType.PICKUP)).collect(Collectors.toList());

        for (SharedCourse sharedCourse : sharedCourses) {
            SharedCourseListUtils.removeAVCourse(sharedMenu, sharedCourse);
        }

        int currentIndex = 0;
        Coord nextCoord = startCoord;
        while (!sharedCourses.isEmpty()) {
            SharedCourse closestCourse = StaticHelper.getClosestCourse(sharedCourses, nextCoord);
            SharedCourseListUtils.addAVCourseAtIndex(sharedMenu, closestCourse, currentIndex);
            currentIndex++;
            nextCoord = closestCourse.getLink().getCoord();
            sharedCourses.remove(closestCourse);
        }

        GlobalAssert.that(originalSize == sharedMenu.size());
        return sharedMenu;
    }
    
    

}
