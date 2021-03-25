/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.fifs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;
import amodeus.amodeus.routing.CachedNetworkTimeDistance;
import amodeus.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

/** A {@link SharedAvRoute} is a list of {@link SharedRoutePoint}s.
 * It is similar to a {@link SharedMenu} in the Robotaxi.
 * But it has more information stored such as the predicted travel time. */
/* package */ class SharedAvRoute {
    public static SharedAvRoute of( //
            List<Directive> list, Link currentLink, //
            double now, double pickupTime, double dropofftime, //
            CachedNetworkTimeDistance timeDb) {
        List<SharedRoutePoint> routePoints = new ArrayList<>();
        Scalar departureTime = Quantity.of(now, SI.SECOND);
        for (int i = 0; i < list.size(); i++) {
            double stopDuration = getStopDuration(list.get(i), pickupTime, dropofftime);
            Link fromLink = (i == 0) ? currentLink : Directive.getLink(list.get(i - 1));
            Link toLink = Directive.getLink(list.get(i));
            Scalar driveTime = timeDb.travelTime(fromLink, toLink, now);
            // TODO @ChengQi after checking with Jan,
            // If the speed becomes to low in the future, here we could improve it by checking
            // the constraints here already to abort a route generation if the constraints are not fulfilled.
            SharedRoutePoint sharedRoutePoint = new SharedRoutePoint(list.get(i), departureTime.add(driveTime).number().doubleValue(), stopDuration);
            routePoints.add(sharedRoutePoint);
            departureTime = Quantity.of(sharedRoutePoint.getEndTime(), SI.SECOND);
        }
        return SharedAvRoute.of(routePoints);
    }

    /** Creates a Shared Menu which is consistent in itself (e.g. no coureses appear twice, for each request it is secured that the dropoff happens after the
     * pickup
     * 
     * @param list of {@link SharedCourse}
     * @return */
    private static SharedAvRoute of(List<SharedRoutePoint> list) {
        // GlobalAssert.that(SharedMenuCheck.coursesAppearOnce(castToCourseList(list)));
        // GlobalAssert.that(SharedMenuCheck.eachPickupAfterDropoff(castToCourseList(list)));
        return new SharedAvRoute(list);
    }

    // ---
    private final List<SharedRoutePoint> route;

    private SharedAvRoute(List<SharedRoutePoint> list) {
        route = Collections.unmodifiableList((Objects.isNull(list)) ? new ArrayList<>() : list);
    }

    public List<SharedRoutePoint> getRoute() {
        return route;
    }

    /** @return an unmodifiable view of the menu */
    public List<Directive> getRoboTaxiMenu() {
        return castToCourseList(route);
    }

    private static double getStopDuration(Directive directive, double pickupDuration, double dropoFfDuration) {
        if (directive instanceof StopDirective) {
            StopDirective stopDirective = (StopDirective) directive;
            
            if (stopDirective.isPickup()) {
                return pickupDuration;
            } else {
                return dropoFfDuration;
            }
        }
        
        return 0.0;
    }

    private static List<Directive> castToCourseList(List<SharedRoutePoint> list) {
        return list.stream().map(i -> i.getDirective()).collect(Collectors.toList());
    }

    public double getEndTime() {
        return route.get(route.size() - 1).getEndTime();
    }

}
