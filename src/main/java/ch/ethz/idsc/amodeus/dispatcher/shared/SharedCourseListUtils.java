package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/**
 * This class gives some functionalities on modifiable Lists of Shared AV Courses. It extends normal List functionalities with some Tests
 * @author Lukas Sieber
 *
 */
public enum SharedCourseListUtils {
    ;
    

    public static List<SharedCourse> copy(List<SharedCourse> courses) {
        return new ArrayList<>(courses);
    }

    // **************************************************
    // ADDING COURSES
    // **************************************************
    public static void addAVCourseAsStarter(List<SharedCourse> courses, SharedCourse avCourse) {
        addAVCourseAtIndex(courses, avCourse, 0);
    }

    public static void addAVCourseAsDessert(List<SharedCourse> courses, SharedCourse avCourse) {
        addAVCourseAtIndex(courses, avCourse, courses.size());
    }

    public static void addAVCourseAtIndex(List<SharedCourse> courses, SharedCourse avCourse, int courseIndex) {
        GlobalAssert.that(0 <= courseIndex && courseIndex <= courses.size());
        courses.add(courseIndex, avCourse);
    }

    // **************************************************
    // MOVING COURSES
    // **************************************************

    public static boolean moveAVCourseToPrev(List<SharedCourse> courses, SharedCourse sharedAVCourse) {
        GlobalAssert.that(courses.contains(sharedAVCourse));
        int i = courses.indexOf(sharedAVCourse);
        if (0 < i && i < courses.size()) {
            Collections.swap(courses, i, i - 1);
            return true;
        }
        System.out.println("Swaping Failed!!");
        return false;
    }

    public static boolean moveAVCourseToNext(List<SharedCourse> courses, SharedCourse sharedAVCourse) {
        GlobalAssert.that(courses.contains(sharedAVCourse));
        int i = courses.indexOf(sharedAVCourse);
        if (0 <= i && i < courses.size() - 1) {
            Collections.swap(courses, i, i + 1);
            return true;
        }
        System.out.println("Swaping Failed!!");
        return false;
    }

    // **************************************************
    // REMOVING COURSES
    // **************************************************

    public static void removeStarterCourse(List<SharedCourse> courses) {
        GlobalAssert.that(!courses.isEmpty());
        courses.remove(0);
    }

    public static void removeAVCourse(List<SharedCourse> courses, int courseIndex) {
        GlobalAssert.that(courses.size() > courseIndex);
        courses.remove(courseIndex);
    }

    public static void removeAVCourse(List<SharedCourse> courses, SharedCourse sharedAVCourse) {
        GlobalAssert.that(courses.contains(sharedAVCourse));
        courses.remove(sharedAVCourse);
    }

}
