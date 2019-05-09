package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.HashSet;
import java.util.List;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum SharedMenuCheck {
    ;

    public static boolean consistencyCheck(List<? extends SharedCourse> courses) {
        return checkAllCoursesAppearOnlyOnce(courses) //
                && checkNoPickupAfterDropoffOfSameRequest(courses);
    }

    public static boolean checkAllCoursesAppearOnlyOnce(List<? extends SharedCourse> courses) {
        return new HashSet<>(courses).size() == courses.size();
    }

    public static boolean checkMenuConsistency(List<? extends SharedCourse> courses, int capacity) {
        return Compatibility.of(courses).forCapacity(capacity);
    }

    /** @return false if any dropoff occurs after pickup in the menu.
     *         If no dropoff ocurs for one pickup an exception is thrown as this is not allowed in a {@link SharedMenu} */
    public static boolean checkNoPickupAfterDropoffOfSameRequest(List<? extends SharedCourse> courses) {
        for (SharedCourse course : courses) {
            if (course.getMealType().equals(SharedMealType.PICKUP)) {
                int pickupIndex = courses.indexOf(course);
                SharedCourse dropoffCourse = SharedCourse.dropoffCourse(course.getAvRequest());
                GlobalAssert.that(courses.contains(dropoffCourse));
                int dropofIndex = courses.indexOf(dropoffCourse);
                if (pickupIndex > dropofIndex) {
                    System.err.println("The SharedRoboTaxiMenu contains a pickup after its dropoff. Stopping Execution.");
                    return false;
                }
            }
        }
        return true;
    }

}
