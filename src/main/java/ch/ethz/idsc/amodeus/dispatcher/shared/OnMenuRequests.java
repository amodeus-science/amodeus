/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.matsim.amodeus.dvrp.request.AVRequest;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum OnMenuRequests {
    ;

    public static Set<AVRequest> getOnBoardRequests(List<? extends SharedCourse> courses) {
        Set<AVRequest> pickups = courseMealStream(courses, SharedMealType.PICKUP) //
                .map(SharedCourse::getAvRequest).collect(Collectors.toSet());

        Set<AVRequest> dropoffs = courseMealStream(courses, SharedMealType.DROPOFF) //
                .map(SharedCourse::getAvRequest).collect(Collectors.toSet());

        for (AVRequest avRequestIDpickup : pickups) {
            boolean removeOk = dropoffs.remove(avRequestIDpickup);
            GlobalAssert.that(removeOk);
        }
        GlobalAssert.that(getOnBoardCustomers(courses) == dropoffs.size());
        return dropoffs;
    }

    // ---

    /** @return number of passengers currently in the {@link List}
     *         of {@link SharedCourse}s @param courses */
    public static long getOnBoardCustomers(List<? extends SharedCourse> courses) {
        return getNumberMealTypes(courses, SharedMealType.DROPOFF) - //
                getNumberMealTypes(courses, SharedMealType.PICKUP);
    }

    public static boolean canPickupAdditionalCustomer(RoboTaxi roboTaxi) {
        int onBoard = (int) roboTaxi.getOnBoardPassengers();
        return onBoard < roboTaxi.getCapacity();
    }

    /** @return number of {@link SharedCourse}s @param courses
     *         with {@link SharedMealType} @param sharedMealType */
    public static long getNumberMealTypes(List<? extends SharedCourse> courses, SharedMealType type) {
        return courseMealStream(courses, type).count();
    }

    /** @return {@link Stream} of all {@link SharedCourse}s @param courses
     *         with {@link SharedMealType} @param sharedMealType */
    private static Stream<? extends SharedCourse> courseMealStream(List<? extends SharedCourse> courses, //
            SharedMealType sharedMealType) {
        return courses.stream().filter(sc -> sc.getMealType().equals(sharedMealType));
    }
}
