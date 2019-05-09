package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.List;
import java.util.function.BiConsumer;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum SharedCourseAdd {
    ;

    // TODO unify these two.
    public static void asStarter(List<SharedCourse> courses, SharedCourse avCourse) {
        atIndex(courses, avCourse, 0);
    }
    
    public static SharedMenu addAVCoursesAsStarter(SharedMenu sharedMenu, SharedCourse... avCourses) {
        return applyFunction(sharedMenu, SharedCourseAdd::asStarter, avCourses);
    }
    // -- 
    

    
    
    // TODO  unify these two.
    public static void asDessert(List<SharedCourse> courses, SharedCourse avCourse) {
        atIndex(courses, avCourse, courses.size());
    }
    
    public static SharedMenu addAVCoursesAsDessert(SharedMenu sharedMenu, SharedCourse... avCourses) {
        return applyFunction(sharedMenu, SharedCourseAdd::asDessert, avCourses);
    }
    // -- 

    
    
    // TODO  unify these two.
    public static void atIndex(List<SharedCourse> courses, SharedCourse avCourse, int courseIndex) {
        GlobalAssert.that(0 <= courseIndex && courseIndex <= courses.size());
        courses.add(courseIndex, avCourse);
    }

    public static SharedMenu addAVCoursesAtIndex(SharedMenu sharedMenu, int courseIndex, SharedCourse... avCourses) {
        List<SharedCourse> list = SharedCourseListUtils.copy(sharedMenu.getRoboTaxiMenu());
        for (SharedCourse sharedCourse : avCourses) {
            SharedCourseAdd.atIndex(list, sharedCourse, courseIndex);
        }
        return SharedMenu.of(list);
    }
    // -- 

    private static SharedMenu applyFunction(SharedMenu sharedMenu, BiConsumer<List<SharedCourse>, SharedCourse> listFunction, SharedCourse... sharedCourses) {
        List<SharedCourse> list = SharedCourseListUtils.copy(sharedMenu.getRoboTaxiMenu());
        for (SharedCourse sharedCourse : sharedCourses) {
            listFunction.accept(list, sharedCourse);
        }
        return SharedMenu.of(list);
    }

}
