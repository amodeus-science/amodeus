package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum SharedCourseMove {
    ;
    
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
    // MOVING COURSES
    // **************************************************

    public static SharedMenu moveAVCourseToPrev(SharedMenu sharedMenu, SharedCourse sharedAVCourse) {
        return applyFunction(sharedMenu, SharedCourseMove::moveAVCourseToPrev, sharedAVCourse);
    }

    public static SharedMenu moveAVCourseToNext(SharedMenu sharedMenu, SharedCourse sharedAVCourse) {
        return applyFunction(sharedMenu, SharedCourseMove::moveAVCourseToNext, sharedAVCourse);
    }

    
    
    private static SharedMenu applyFunction(SharedMenu sharedMenu, BiConsumer<List<SharedCourse>, SharedCourse> listFunction, SharedCourse... sharedCourses) {
        List<SharedCourse> list = SharedCourseListUtils.copy(sharedMenu.getRoboTaxiMenu());
        for (SharedCourse sharedCourse : sharedCourses) {
            listFunction.accept(list, sharedCourse);
        }
        return SharedMenu.of(list);
    }


}
