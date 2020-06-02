/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import amodeus.amodeus.util.math.GlobalAssert;

public enum SharedCourseMove {
    ;

    public static SharedMenu moveAVCourseToPrev(SharedMenu sharedMenu, SharedCourse sharedAVCourse) {
        return applyFunction(sharedMenu, SharedCourseMove::moveAVCourseToPrev, sharedAVCourse);
    }

    public static SharedMenu moveAVCourseToNext(SharedMenu sharedMenu, SharedCourse sharedAVCourse) {
        return applyFunction(sharedMenu, SharedCourseMove::moveAVCourseToNext, sharedAVCourse);
    }

    // --

    private static SharedMenu applyFunction(SharedMenu sharedMenu, BiConsumer<List<SharedCourse>, SharedCourse> listFunction, //
            SharedCourse... sharedCourses) {
        List<SharedCourse> list = SharedCourseUtil.copy(sharedMenu.getCourseList());
        for (SharedCourse sharedCourse : sharedCourses)
            listFunction.accept(list, sharedCourse);
        return SharedMenu.of(list);
    }

    private static boolean moveAVCourseToPrev(List<SharedCourse> courses, SharedCourse sharedAVCourse) {
        GlobalAssert.that(courses.contains(sharedAVCourse));
        int i = courses.indexOf(sharedAVCourse);
        if (0 < i && i < courses.size()) {
            Collections.swap(courses, i, i - 1);
            return true;
        }
        System.out.println("Swapping Failed!!");
        return false;
    }

    private static boolean moveAVCourseToNext(List<SharedCourse> courses, SharedCourse sharedAVCourse) {
        GlobalAssert.that(courses.contains(sharedAVCourse));
        int i = courses.indexOf(sharedAVCourse);
        if (0 <= i && i < courses.size() - 1) {
            Collections.swap(courses, i, i + 1);
            return true;
        }
        System.out.println("Swapping Failed!!");
        return false;
    }

}
