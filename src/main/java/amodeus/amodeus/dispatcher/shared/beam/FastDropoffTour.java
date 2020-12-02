/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.beam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;

import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.DriveDirective;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;
import amodeus.amodeus.util.math.GlobalAssert;

/* package */ enum FastDropoffTour {
    ;

    public static List<Directive> fastDropoffTour(List<Directive> unmodifiableSharedMenu) {
        List<Directive> sharedCourses = new ArrayList<>(unmodifiableSharedMenu);
        GlobalAssert.that(StaticHelper.checkAllPickupsFirst(sharedCourses));
        GlobalAssert.that(sharedCourses.stream().noneMatch(FastDropoffTour::isDrive));

        Coord lastPickupCoord = Directive.getLink(getLastPickup(sharedCourses)).getCoord();
        Set<Directive> set = sharedCourses.stream().filter(FastDropoffTour::isDropoff).collect(Collectors.toSet());

        sharedCourses.removeAll(set);

        final int iter = set.size();
        for (int i = 0; i < iter; i++) {
            Directive sharedCourse = StaticHelper.getClosestCourse(set, lastPickupCoord);
            sharedCourses.add(sharedCourse);
            set.remove(sharedCourse);
        }
        GlobalAssert.that(set.isEmpty());
        return sharedCourses;
    }
    
    public static boolean isPickup(Directive directive) {
        if (directive instanceof StopDirective) {
            StopDirective stopDirective = (StopDirective) directive;
            return stopDirective.isPickup();
        }
        
        return false;
    }
    
    public static boolean isDropoff(Directive directive) {
        if (directive instanceof StopDirective) {
            StopDirective stopDirective = (StopDirective) directive;
            return !stopDirective.isPickup();
        }
        
        return false;
    }
    
    public static boolean isDrive(Directive directive) {
        return directive instanceof DriveDirective;
    }

    private static Directive getLastPickup(List<Directive> sharedMenu) {
        return sharedMenu.stream().filter(FastDropoffTour::isPickup).reduce((c1, c2) -> c2).orElse(null);
    }
}
