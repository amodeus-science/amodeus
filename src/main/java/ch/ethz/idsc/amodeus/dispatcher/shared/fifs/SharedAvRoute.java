/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

/** A {@link SharedAvRoute} is a list of {@link SharedRoutePoint}s.
 * It is similar to a {@link SharedMenu} in the Robotaxi.
 * But it has more information stored such as the predicted travel time. */
/* package */ class SharedAvRoute {
    static SharedAvRoute of( //
            List<SharedCourse> list, Link currentLink, //
            double now, double pickupTime, double dropofftime, //
            TravelTimeCalculatorCached timeDb) {
        List<SharedRoutePoint> routePoints = new ArrayList<>();
        Scalar departureTime = Quantity.of(now, SI.SECOND);
        for (int i = 0; i < list.size(); i++) {
            double stopDuration = getStopDuration(list.get(i).getMealType(), pickupTime, dropofftime);
            Link fromLink = (i == 0) ? currentLink : list.get(i - 1).getLink();
            Link toLink = list.get(i).getLink();
            Scalar driveTime = timeDb.timeFromTo(fromLink, toLink);
            // TODO If the speed becomes to low in the future, here we could improve it by checking
            // the constraints here already to abort a route generation if the constraints are not fulfilled.
            SharedRoutePoint sharedRoutePoint = new SharedRoutePoint(list.get(i), departureTime.add(driveTime).number().doubleValue(), stopDuration);
            routePoints.add(sharedRoutePoint);
            departureTime = Quantity.of(sharedRoutePoint.getEndTime(), SI.SECOND);
        }
        return SharedAvRoute.of(routePoints);
    }

    /** Creates a Shared Menu which is consistent in itself (e.g. no coureses appear twice, for each request it is secured that the dropoff happens after the pickup
     * 
     * @param list of {@link SharedCourse}
     * @return */
    private static SharedAvRoute of(List<SharedRoutePoint> list) {
        GlobalAssert.that(SharedCourseListUtils.consistencyCheck(castToCourseList(list)));
        return new SharedAvRoute(list);
    }

    // ---
    private final List<SharedRoutePoint> route;

    private SharedAvRoute(List<SharedRoutePoint> list) {
        route = Collections.unmodifiableList((Objects.isNull(list)) ? new ArrayList<>() : list);
    }

    List<SharedRoutePoint> getRoute() {
        return route;
    }

    /** @return an unmodifiable view of the menu */
    List<SharedCourse> getRoboTaxiMenu() {
        return castToCourseList(route);
    }

    private static double getStopDuration(SharedMealType sharedMealType, double pickupDuration, double dropoFfDuration) {
        switch (sharedMealType) {
        case PICKUP:
            return pickupDuration;
        case DROPOFF:
            return dropoFfDuration;
        default:
            return 0.0;
        }
    }

    private static List<SharedCourse> castToCourseList(List<SharedRoutePoint> list) {
        return new ArrayList<>(list);
    }

    Double getEndTime() {
        return route.get(route.size() - 1).getEndTime();
    }

}
