/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.backup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

public enum SharedCourseUtil {
    ;

    /** @return deep copy of {@link List} of {@link SharedCourse}s @param courses */
    public static List<SharedCourse> copy(List<SharedCourse> courses) {
        return new ArrayList<>(courses);
    }

    /** @return {@link Set} of unique {@link PassengerRequest}s in the {@link List} of {@link SharedCourse}s
     *         provided in @param courses */
    public static Set<PassengerRequest> getUniquePassengerRequests(List<? extends SharedCourse> courses) {
        return courses.stream() //
                .filter(sc -> !sc.getMealType().equals(SharedMealType.REDIRECT)) //
                .map(SharedCourse::getAvRequest).collect(Collectors.toSet()); //
    }

}
