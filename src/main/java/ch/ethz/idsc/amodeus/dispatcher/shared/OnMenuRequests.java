package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

public enum OnMenuRequests {
    ;

    public static Set<AVRequest> getOnBoardRequests(List<? extends SharedCourse> courses) {
        Set<AVRequest> pickups = courseMealStream(courses, SharedMealType.PICKUP)//
                .map(sc -> sc.getAvRequest()).collect(Collectors.toSet());

        Set<AVRequest> dropoffs = courseMealStream(courses, SharedMealType.DROPOFF)//
                .map(sc -> sc.getAvRequest()).collect(Collectors.toSet());

        for (AVRequest avRequestIDpickup : pickups) {
            boolean removeOk = dropoffs.remove(avRequestIDpickup);
            GlobalAssert.that(removeOk);
        }
        GlobalAssert.that(getOnBoardCustomers(courses) == dropoffs.size());
        return dropoffs;
    }

    // ---

    public static long getOnBoardCustomers(List<? extends SharedCourse> courses) {
        long onBoard = getNumberMealTypes(courses, SharedMealType.DROPOFF) - //
                getNumberMealTypes(courses, SharedMealType.PICKUP);
        return onBoard;
    }

    public static boolean canPickupNewCustomer(RoboTaxi roboTaxi) {
        int onBoard = (int) roboTaxi.getMenuOnBoardCustomers();
        GlobalAssert.that(onBoard >= 0);
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
