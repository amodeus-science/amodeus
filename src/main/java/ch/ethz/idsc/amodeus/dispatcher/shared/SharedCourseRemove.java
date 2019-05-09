package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum SharedCourseRemove {
    ;

    public static void removeStarterCourse(List<SharedCourse> courses) {
        GlobalAssert.that(!courses.isEmpty());
        courses.remove(0);
    }

    public static SharedMenu removeStarterCourse(SharedMenu sharedMenu) {
        return applyFunction(sharedMenu, SharedCourseRemove::removeStarterCourse);
    }

    public static void removeAVCourse(List<SharedCourse> courses, int courseIndex) {
        GlobalAssert.that(courses.size() > courseIndex);
        courses.remove(courseIndex);
    }

    public static SharedMenu removeAVCourse(SharedMenu sharedMenu, int courseIndex) {
        return applyFunction(sharedMenu, courseIndex, SharedCourseRemove::removeAVCourse);
    }

    public static void removeAVCourse(List<SharedCourse> courses, SharedCourse sharedAVCourse) {
        GlobalAssert.that(courses.contains(sharedAVCourse));
        courses.remove(sharedAVCourse);
    }

    public static SharedMenu removeAVCourses(SharedMenu sharedMenu, SharedCourse... sharedAVCourses) {
        return applyFunction(sharedMenu, SharedCourseRemove::removeAVCourse, sharedAVCourses);
    }

    // --

    private static SharedMenu applyFunction(SharedMenu sharedMenu, Consumer<List<SharedCourse>> listFunction) {
        List<SharedCourse> list = SharedCourseListUtils.copy(sharedMenu.getRoboTaxiMenu());
        listFunction.accept(list);
        return SharedMenu.of(list);
    }

    private static SharedMenu applyFunction(SharedMenu sharedMenu, BiConsumer<List<SharedCourse>, SharedCourse> listFunction, SharedCourse... sharedCourses) {
        List<SharedCourse> list = SharedCourseListUtils.copy(sharedMenu.getRoboTaxiMenu());
        for (SharedCourse sharedCourse : sharedCourses) {
            listFunction.accept(list, sharedCourse);
        }
        return SharedMenu.of(list);
    }

    private static SharedMenu applyFunction(SharedMenu sharedMenu, Integer integer, BiConsumer<List<SharedCourse>, Integer> listFunction) {
        List<SharedCourse> list = SharedCourseListUtils.copy(sharedMenu.getRoboTaxiMenu());
        listFunction.accept(list, integer);
        return SharedMenu.of(list);
    }

}
