/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.beam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;

import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.util.math.GlobalAssert;

/* package */ enum FastPickupTour {
    ;

    public static List<Directive> fastPickupTour(List<Directive> unmodifiableSharedMenu, Coord startCoord) {
        List<Directive> originalSharedCourses = new ArrayList<>(unmodifiableSharedMenu);

        GlobalAssert.that(StaticHelper.checkAllPickupsFirst(originalSharedCourses));
        final int originalSize = originalSharedCourses.size();
        Collection<Directive> sharedCourses = originalSharedCourses.stream() //
                .filter(FastDropoffTour::isPickup).collect(Collectors.toList());

        originalSharedCourses.removeAll(sharedCourses);

        int currentIndex = 0;
        Coord nextCoord = startCoord;
        while (!sharedCourses.isEmpty()) {
            Directive closestCourse = StaticHelper.getClosestCourse(sharedCourses, nextCoord);
            originalSharedCourses.add(currentIndex, closestCourse);
            currentIndex++;
            nextCoord = Directive.getLink(closestCourse).getCoord();
            sharedCourses.remove(closestCourse);
        }

        GlobalAssert.that(originalSize == originalSharedCourses.size());
        return originalSharedCourses;
    }
}
