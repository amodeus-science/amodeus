package ch.ethz.idsc.amodeus.dispatcher.shared.beam;

import java.util.Collection;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.network.NetworkUtils;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ enum StaticHelper {
    ;

    public static boolean checkAllPickupsFirst(List<SharedCourse> sharedMenu) {
        boolean justPickupsSoFar = true;
        for (SharedCourse sharedCourse : sharedMenu) {
            if (sharedCourse.getMealType().equals(SharedMealType.PICKUP)) {
                if (!justPickupsSoFar) {
                    return false;
                }
            } else {
                justPickupsSoFar = false;
            }
        }
        return true;
    }

    public static SharedCourse getClosestCourse(Collection<SharedCourse> sharedCourses, Coord coord) {
        GlobalAssert.that(!sharedCourses.isEmpty());
        SharedCourse closestCourse = null;
        Double distance = null;
        for (SharedCourse sharedCourse : sharedCourses) {
            double d = NetworkUtils.getEuclideanDistance(sharedCourse.getLink().getCoord(), coord);

            if (closestCourse == null) {
                closestCourse = sharedCourse;
                distance = d;
            } else {
                if (d < distance) {
                    distance = d;
                    closestCourse = sharedCourse;
                }
            }
        }
        return closestCourse;
    }

}
