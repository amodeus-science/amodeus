/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.kockelman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class SharedAvRoute {
    private final List<SharedRoutePoint> route;

    /** Creates a Shared Menu which is consistent in itself (e.g. no coureses appear twice, for each request it is secured that the dropoff happens after the pickup
     * 
     * @param list of {@link SharedCourse}
     * @return */
    private static SharedAvRoute of(List<SharedRoutePoint> list) {
        GlobalAssert.that(SharedCourseListUtils.consistencyCheck(castToCourseList(list)));
        return new SharedAvRoute(list);
    }

    private SharedAvRoute(List<SharedRoutePoint> list) {
        route = Collections.unmodifiableList((Objects.isNull(list)) ? new ArrayList<>() : list);
    }

    /* package */ List<SharedRoutePoint> getRoute() {
        return route;
    }

    /** @return an unmodifiable view of the menu */
    /* package */ List<SharedCourse> getRoboTaxiMenu() {
        return castToCourseList(route);
    }

    /* package */ static SharedAvRoute of(List<SharedCourse> list, Link currentLink, double now, double pickupTime, double dropofftime, TravelTimeCalculatorCached timeDb) {
        List<SharedRoutePoint> routePoints = new ArrayList<>();
        Scalar departureTime = Quantity.of(now, SI.SECOND);
        for (int i = 0; i < list.size(); i++) {
            double stopDuration = getStopDuration(list.get(i).getMealType(), pickupTime, dropofftime);
            Link fromLink = (i == 0) ? currentLink : list.get(i - 1).getLink();
            Link toLink = list.get(i).getLink();
            Scalar driveTime = timeDb.timeFromTo(fromLink, toLink);
            SharedRoutePoint sharedRoutePoint = new SharedRoutePoint(list.get(i), departureTime.add(driveTime).number().doubleValue(), stopDuration);
            routePoints.add(sharedRoutePoint);
            departureTime = Quantity.of(sharedRoutePoint.getEndTime(), SI.SECOND);
        }
        return SharedAvRoute.of(routePoints);
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

    /* package */ Double getEndTime() {
        return route.get(route.size() - 1).getEndTime();
    }

}
