/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.List;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/** Checks if a {@link SharedMenu} for a {@link RoboTaxi} is compatible with its
 * capacity, i.e., if at no point in time there are more passengers on board that
 * the vehicle can host. Usage:
 * 
 * 1) Compatibility.of(courses).forCapacity(c) or
 * 2) Compatibility.of(roboTaxi).forCapacity(c)
 * 
 * 
 * The second usage is preferred as it is computationally more efficient. */
public class Compatibility {

    private final List<? extends SharedCourse> courses;
    private long initialOnBoard;

    public static Compatibility of(List<? extends SharedCourse> courses) {
        return new Compatibility(courses);
    }

    public static Compatibility of(RoboTaxi roboTaxi) {
        return new Compatibility(roboTaxi);
    }

    // private constructors

    private Compatibility(List<? extends SharedCourse> courses) {
        this.courses = courses;
        initialOnBoard = OnMenuRequests.getOnBoardCustomers(courses);
    }

    private Compatibility(RoboTaxi roboTaxi) {
        this.courses = roboTaxi.getUnmodifiableViewOfCourses();
        initialOnBoard = roboTaxi.getMenuOnBoardCustomers();// OnMenuRequests.getOnBoardCustomers(courses);
    }

    /** @param capacity maximum number of seats in the taxi.
     * @return true if the maximum number of seats is never violated with the menu {@link courses}. */
    public boolean forCapacity(int capacity) {
        long onBoardPassengers = initialOnBoard;
        for (SharedCourse course : courses) {
            if (course.getMealType().equals(SharedMealType.PICKUP)) {
                onBoardPassengers++;
            } else if (course.getMealType().equals(SharedMealType.DROPOFF)) {
                onBoardPassengers--;
            } else if (course.getMealType().equals(SharedMealType.REDIRECT)) {
                // --
            } else {
                throw new IllegalArgumentException("Unknown SharedAVMealType -- please specify it !!!--");
            }
            if (onBoardPassengers > capacity) {
                return false;
            }
        }
        return true;
    }
}
