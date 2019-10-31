/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.beam;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;

/* package */ enum Reorder {
    ;

    /** Changes order of the menu such that first all pickups and then all dropoffs occur.
     * The order is kept. The Redirect Courses are put at the end */
    public static List<SharedCourse> firstAllPickupsThenDropoffs(List<SharedCourse> roboTaxiMenu) {
        List<SharedCourse> list = appendType(roboTaxiMenu, new ArrayList<>(), SharedMealType.PICKUP);
        // add PickupCourses
        /* list = */ appendType(roboTaxiMenu, list, SharedMealType.DROPOFF);
        return appendType(roboTaxiMenu, list, SharedMealType.REDIRECT);
    }

    private static List<SharedCourse> appendType(List<SharedCourse> orig, List<SharedCourse> dest, SharedMealType type) {
        orig.stream().filter(c -> c.getMealType().equals(type)).forEachOrdered(dest::add);
        return dest;
    }
}
