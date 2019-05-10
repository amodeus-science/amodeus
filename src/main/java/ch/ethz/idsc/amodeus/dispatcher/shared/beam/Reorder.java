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
        List<SharedCourse> list = new ArrayList<>();
        // add PickupCourses
        for (SharedCourse sharedCourse : roboTaxiMenu) {
            if (sharedCourse.getMealType().equals(SharedMealType.PICKUP)) {
                list.add(sharedCourse);
            }
        }
        for (SharedCourse sharedCourse : roboTaxiMenu) {
            if (sharedCourse.getMealType().equals(SharedMealType.DROPOFF)) {
                list.add(sharedCourse);
            }
        }
        for (SharedCourse sharedCourse : roboTaxiMenu) {
            if (sharedCourse.getMealType().equals(SharedMealType.REDIRECT)) {
                list.add(sharedCourse);
            }
        }
        return list;
    }

}
