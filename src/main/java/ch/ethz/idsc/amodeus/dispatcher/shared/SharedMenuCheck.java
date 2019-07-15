/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.HashSet;
import java.util.List;

/** Class used to perform certain checks on {@link SharedMenu}s */
public enum SharedMenuCheck {
    ;

    /** @return true of every {@link SharedCourse} appears exactly once in the
     *         {@link List} @param courses, otherwise @return false */
    public static boolean coursesAppearOnce(List<? extends SharedCourse> courses) {
        return new HashSet<>(courses).size() == courses.size();
    }

    /** @return true of @param sharedMenu1 and @param sharedMenu2 are of same size and
     *         all courses contained in @param sharedMenu1 are contained in @param sharedMenu2 and vice
     *         versa, otherwise @return false */
    public static boolean containSameCourses(SharedMenu sharedMenu1, SharedMenu sharedMenu2) {
        boolean sizeSame = sharedMenu1.getCourseList().size() == sharedMenu2.getCourseList().size();
        boolean twoInOne = sharedMenu1.getCourseList().containsAll(sharedMenu2.getCourseList());
        boolean oneInTwo = sharedMenu2.getCourseList().containsAll(sharedMenu1.getCourseList());
        return sizeSame && twoInOne && oneInTwo;
    }

    /** @return true if for every {@link SharedCourse} of {@link SharedMealType} pickup
     *         exactly one {@link SharedCourse} of {@link SharedMealType} dropoff
     *         appears in @param courses */
    public static boolean eachPickupAfterDropoff(List<? extends SharedCourse> courses) {
        for (SharedCourse course : courses) {
            if (course.getMealType().equals(SharedMealType.PICKUP)) {
                int pickupIndex = courses.indexOf(course);
                SharedCourse dropoffCourse = SharedCourse.dropoffCourse(course.getAvRequest());
                if (!courses.contains(dropoffCourse)) {
                    System.err.println("SharedRoboTaxiMenu contains pickup but no dropoff.");
                    return false;
                }
                if (pickupIndex > courses.indexOf(dropoffCourse)) {
                    System.err.println("SharedRoboTaxiMenu contains a pickup after its dropoff.");
                    return false;
                }
            }
        }
        return true;
    }

}
