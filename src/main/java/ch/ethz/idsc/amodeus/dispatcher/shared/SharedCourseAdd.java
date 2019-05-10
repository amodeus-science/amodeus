package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.List;
import java.util.function.BiConsumer;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** Class used to add new {@link SharedCourse}s to a {@link SharedMenu} */
public enum SharedCourseAdd {
    ;

    /** @return {@link SharedMenu} @param sharedMenu in which the {@link SharedCourse}s @param avCourses
     *         are inserted at the beginning (as starters...) */
    public static SharedMenu asStarter(SharedMenu sharedMenu, SharedCourse... avCourses) {
        List<SharedCourse> list = SharedCourseUtil.copy(sharedMenu.getCourseList());
        return updated(list, SharedCourseAdd::asStarterList, avCourses);
    }

    public static void asStarterList(List<SharedCourse> courses, SharedCourse avCourse) {
        atIndexList(courses, avCourse, 0);
    }

    /** @return {@link SharedMenu} @param sharedMenu in which the {@link SharedCourse}s @param avCourses
     *         are inserted at the end (as desserts...) */
    public static SharedMenu asDessert(SharedMenu sharedMenu, SharedCourse... avCourses) {
        List<SharedCourse> list = SharedCourseUtil.copy(sharedMenu.getCourseList());
        return updated(list, SharedCourseAdd::asDessertList, avCourses);
    }

    public static void asDessertList(List<SharedCourse> courses, SharedCourse avCourse) {
        atIndexList(courses, avCourse, courses.size());
    }

    /** @return {@link SharedMenu} @param sharedMenu in which the {@link SharedCourse}s @param avCourses
     *         are inserted starting at the position @param courseIndex */
    public static SharedMenu atIndex(SharedMenu sharedMenu, int courseIndex, SharedCourse... avCourses) {
        List<SharedCourse> list = SharedCourseUtil.copy(sharedMenu.getCourseList());
        for (SharedCourse sharedCourse : avCourses)
            SharedCourseAdd.atIndexList(list, sharedCourse, courseIndex);
        return SharedMenu.of(list);
    }

    public static void atIndexList(List<SharedCourse> courses, SharedCourse avCourse, int courseIndex) {
        GlobalAssert.that(0 <= courseIndex && courseIndex <= courses.size());
        courses.add(courseIndex, avCourse);
    }

    // -- internal

    private static SharedMenu updated(List<SharedCourse> list, BiConsumer<List<SharedCourse>, SharedCourse> listFunction, //
            SharedCourse... sharedCourses) {
        for (SharedCourse sharedCourse : sharedCourses)
            listFunction.accept(list, sharedCourse);
        return SharedMenu.of(list);
    }

}
