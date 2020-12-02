/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.beam;

import java.util.Collection;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.network.NetworkUtils;

import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;
import amodeus.amodeus.util.math.GlobalAssert;

/* package */ enum StaticHelper {
    ;

    public static boolean checkAllPickupsFirst(List<Directive> sharedMenu) {
        boolean justPickupsSoFar = true;
        for (Directive sharedCourse : sharedMenu)
            if (sharedCourse instanceof StopDirective && ((StopDirective) sharedCourse).isPickup()) {
                if (!justPickupsSoFar)
                    return false;
            } else
                justPickupsSoFar = false;
        return true;
    }

    public static Directive getClosestCourse(Collection<Directive> sharedCourses, Coord coord) {
        GlobalAssert.that(!sharedCourses.isEmpty());
        Directive closestCourse = null;
        Double distance = null;
        for (Directive sharedCourse : sharedCourses) {
            double d = NetworkUtils.getEuclideanDistance(Directive.getLink(sharedCourse).getCoord(), coord);

            if (closestCourse == null) {
                closestCourse = sharedCourse;
                distance = d;
            } else if (d < distance) {
                distance = d;
                closestCourse = sharedCourse;
            }
        }
        return closestCourse;
    }
}
