/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Class to remove {@link SharedCourse}s from a {@link SharedMenu} */
public enum SharedCourseRemove {
    ;

    /** @return {@link SharedMenu} identical to @param sharedMenu without the
     *         first course (the starter course), if the menu is empty, no modification is made */
    public static SharedMenu starter(SharedMenu sharedMenu) {
        List<SharedCourse> courses = SharedCourseUtil.copy(sharedMenu.getCourseList());
        if (!courses.isEmpty())
            courses.remove(0);
        return SharedMenu.of(courses);
    }

    /** @return {@link SharedMenu} identical to @param sharedMenu without the course
     *         at the index @param index, if the index is less than 0 or higher than the
     *         length of the @param sharedMenu, the request is ignored. */
    public static SharedMenu index(SharedMenu sharedMenu, int index) {
        List<SharedCourse> courses = SharedCourseUtil.copy(sharedMenu.getCourseList());
        if (index >= 0 && index < courses.size())
            courses.remove(index);
        return SharedMenu.of(courses);
    }

    /** @return {@link List} of {@link SharedCourse}s identical to @param courses without
     *         the course @param toRemove, if @param toRemove is not contained in @param courses
     *         no modification is made. */
    public static void specific(List<SharedCourse> courses, SharedCourse toRemove) {
        if (courses.contains(toRemove))
            courses.remove(toRemove);
    }

    /** @return {@link SharedMenu} identical to @param sharedMenu without the courses
     *         supplied in @param removeCourses */
    public static SharedMenu several(SharedMenu sharedMenu, SharedCourse... removeCourses) {
        List<SharedCourse> copy = new ArrayList<>();
        List<SharedCourse> toRemove = Arrays.asList(removeCourses);
        for (SharedCourse sharedCourse : sharedMenu.getCourseList()) {
            if (!toRemove.contains(sharedCourse))
                copy.add(sharedCourse);
        }
        return SharedMenu.of(copy);
    }

}
