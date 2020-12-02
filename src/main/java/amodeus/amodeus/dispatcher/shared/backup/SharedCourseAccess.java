/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.backup;

import java.util.List;
import java.util.Optional;

import amodeus.amodeus.dispatcher.core.RoboTaxi;

/** Fast access for {@link SharedCourse}s in a {@link SharedMenu}, most importantly the
 * starter course, i.e., the next on the {@link SharedMenu} of the {@link RoboTaxi} */
public enum SharedCourseAccess {
    ;

    /** @return {@link Optional} {@link SharedCourse} of the first course in the {@link SharedMenu}
     *         of the {@link RoboTaxi} @param roboTaxi */
    public static Optional<SharedCourse> getStarter(RoboTaxi roboTaxi) {
        return null;
        // return SharedCourseAccess.getStarter(roboTaxi.getUnmodifiableViewOfCourses());
    }

    /** @return {@link Optional} {@link SharedCourse} of the first course in the {@link List} of
     *         {@link SharedCourse}s @param courses */
    public static Optional<SharedCourse> getStarter(List<? extends SharedCourse> courses) {
        return Optional.ofNullable((hasStarter(courses)) ? courses.get(0) : null);
    }

    /** @return true if the {@link RoboTaxi} @param roboTaxi has a starter
     *         course, otherwise return false */
    public static boolean hasStarter(RoboTaxi roboTaxi) {
        return false; // hasStarter(roboTaxi.getUnmodifiableViewOfCourses());
    }

    /** @return true if the {@link List} of {@link SharedCourse}s @param courses has a starter
     *         course, otherwise return false */
    public static boolean hasStarter(List<? extends SharedCourse> courses) {
        return !courses.isEmpty();
    }

    /** @return {@link Optional} {@link SharedCourse} of the second course in the {@link List} of
     *         {@link SharedCourse}s @param courses */
    public static Optional<SharedCourse> getSecond(List<SharedCourse> courses) {
        return Optional.ofNullable((hasSecondCourse(courses)) ? courses.get(1) : null);
    }

    /** @return true if the {@link RoboTaxi} @param roboTaxi has a second
     *         course, otherwise return false */
    public static boolean hasSecondCourse(RoboTaxi roboTaxi) {
        return false; // hasSecondCourse(roboTaxi.getUnmodifiableViewOfCourses());
    }

    /** @return true if the {@link List} of {@link SharedCourse}s @param courses has a second
     *         course, otherwise return false */
    public static boolean hasSecondCourse(List<SharedCourse> courses) {
        return hasStarter(courses) && courses.size() >= 2;
    }
}
