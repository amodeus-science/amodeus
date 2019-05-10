/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

public enum SharedCourseListUtils {
    ;

    public static List<SharedCourse> copy(List<SharedCourse> courses) {
        return new ArrayList<>(courses);
    }

    // **************************************************
    // Get Functions
    // **************************************************

    public static Set<AVRequest> getUniqueAVRequests(List<? extends SharedCourse> courses) {
        return courses.stream()//
                .filter(sc -> !sc.getMealType().equals(SharedMealType.REDIRECT))//
                .map(sc -> sc.getAvRequest()).collect(Collectors.toSet());//
    }

    /** Gets the next course of the menu.
     * 
     * @return */
    public static Optional<SharedCourse> getStarterCourse(List<? extends SharedCourse> courses) {
        return Optional.ofNullable((hasStarter(courses)) ? courses.get(0) : null);
    }

    public static Optional<SharedCourse> getStarterCourse(RoboTaxi roboTaxi) {
        return SharedCourseListUtils.getStarterCourse(roboTaxi.getUnmodifiableViewOfCourses());
    }
    
    public static Optional<SharedCourse> getStarterCourse(SharedMenu sharedMenu) {
        return SharedCourseListUtils.getStarterCourse(sharedMenu.getCourseList());
    }
    
    

    public static Optional<SharedCourse> getSecondCourse(List<SharedCourse> courses) {
        return Optional.ofNullable((hasSecondCourse(courses)) ? courses.get(1) : null);
    }

    // **************************************************
    // Check Shared Course List
    // **************************************************

    public static boolean hasStarter(List<? extends SharedCourse> courses) {
        return !courses.isEmpty();
    }

    public static boolean hasSecondCourse(List<SharedCourse> courses) {
        if (hasStarter(courses)) {
            return courses.size() >= 2;
        }
        return false;
    }

}
