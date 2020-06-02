/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared;

import java.util.List;
import java.util.Objects;

import amodeus.amodeus.dispatcher.core.RoboTaxi;

/** Checks if a {@link SharedMenu} for a {@link RoboTaxi} is compatible with its
 * capacity, i.e., if at no point in time there are more passengers on board that
 * the vehicle can host. Usage:
 * 
 * Compatibility.of(courses).forCapacity(c) */
public class Compatibility {

    public static Compatibility of(List<? extends SharedCourse> courses) {
        return new Compatibility(Objects.requireNonNull(courses));
    }

    // ---
    private final List<? extends SharedCourse> courses;

    private Compatibility(List<? extends SharedCourse> courses) {
        this.courses = courses;
    }

    /** @param capacity maximum numbe of seats in the taxi.
     * @return true if the maximum number of seats is never violated with the menu {@link courses}. */
    public boolean forCapacity(int capacity) {
        long onBoardPassengers = OnMenuRequests.getOnBoardCustomers(courses);
        for (SharedCourse course : courses) {
            switch (course.getMealType()) {
            case PICKUP:
                onBoardPassengers++;
                break;
            case DROPOFF:
                onBoardPassengers--;
                break;
            case REDIRECT:
                break;
            default:
                throw new IllegalArgumentException("Unknown SharedAVMealType -- please specify it !!!--");
            }
            if (onBoardPassengers > capacity)
                return false;
        }
        return true;
    }
}
