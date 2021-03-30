/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.fifs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;
import amodeus.amodeus.dispatcher.shared.Compatibility;
import amodeus.amodeus.routing.CachedNetworkTimeDistance;
import amodeus.amodeus.util.math.GlobalAssert;

/* package */ class RouteValidation {

    private final double pickupDuration;
    private final double dropoffDuration;
    private final double maxPickupTime;

    private final RideSharingConstraints rideSharingConstraints;

    public RouteValidation(double maxPickupTime, double maxDriveTimeIncrease, double maxRemainingTimeIncreas, double dropoffTime, double pickupDuration,
            double newTravelerMinIncreaseAllowed) {
        this.maxPickupTime = maxPickupTime;
        this.dropoffDuration = dropoffTime;
        this.pickupDuration = pickupDuration;
        rideSharingConstraints = new RideSharingConstraints(maxPickupTime, maxDriveTimeIncrease, maxRemainingTimeIncreas, dropoffTime, newTravelerMinIncreaseAllowed);
    }

    /** Returns whether a given shared Route fulfills all the 5 constraints or not.
     * This is subject to the old route and the new request.
     * 
     * @param sharedAvRoute
     * @param oldRoute
     * @param newRequestWrap
     * @param now
     * @param requestHandler
     * @return true if this is a valid route, false if the rout can not be considered for sharing. */
    public boolean isValidRoute(SharedAvRoute sharedAvRoute, SharedAvRoute oldRoute, RequestWrap newRequestWrap, double now, RequestHandler requestHandler) {
        Map<PassengerRequest, Double> driveTimes = requestHandler.getDriveTimes(sharedAvRoute);
        PassengerRequest newAvRequest = newRequestWrap.getAvRequest();
        double unitCapacityDriveTime = requestHandler.getDriveTimeDirectUnitCap(newAvRequest);
        GlobalAssert.that(unitCapacityDriveTime == newRequestWrap.getUnitDriveTime());
        // Requirement 1 Current Passenger Total Travel Time Increase
        if (rideSharingConstraints.driveTimeCurrentPassengersExceeded(driveTimes, newAvRequest, requestHandler))
            return false;

        // Requirement 2 Current Passenger remaining Time Increase
        if (rideSharingConstraints.remainingTimeCurrentPassengerExceeded(sharedAvRoute, oldRoute, now))
            return false;

        // Requirement 3 New Passenger Drive Time Increase
        if (rideSharingConstraints.driveTimeNewPassengerExceeded(driveTimes.get(newAvRequest), unitCapacityDriveTime))
            return false;

        // Requirement 4 Pick up within 5 min
        if (rideSharingConstraints.waitTimesExceeded(sharedAvRoute, now))
            return false;

        // Requirement 5 combined constraint
        return rideSharingConstraints.combinedConstraintAcceptable(sharedAvRoute, oldRoute, unitCapacityDriveTime);
    }

    /** @param robotaxisWithMenu
     * @param avRequest
     * @param now
     * @param timeDb
     * @param requestMaintainer
     * @param roboTaxiMaintainer
     * @return The Closest RoboTaxi with a Shared Menu associated with it. */
    /* package */ Optional<Entry<RoboTaxi, List<Directive>>> getClosestValidSharingRoboTaxi(Set<RoboTaxi> robotaxisWithMenu, PassengerRequest avRequest, double now, //
            CachedNetworkTimeDistance timeDb, RequestHandler requestMaintainer, RoboTaxiHandler roboTaxiMaintainer) {

        GlobalAssert.that(robotaxisWithMenu.stream().allMatch(r -> r.getScheduleManager().getDirectives().size() > 0));

        NavigableMap<Double, RoboTaxi> roboTaxisWithinMaxPickup = RoboTaxiUtilsFagnant.getRoboTaxisWithinMaxTime(avRequest.getFromLink(), //
                robotaxisWithMenu, timeDb, maxPickupTime, roboTaxiMaintainer, now);

        AvRouteHandler avRouteHandler = new AvRouteHandler();
        Directive pickupCourse = Directive.pickup(avRequest);
        Directive dropoffCourse = Directive.dropoff(avRequest);
        Map<RoboTaxi, SharedAvRoute> oldRoutes = new HashMap<>();

        // Calculate all routes and times
        for (RoboTaxi roboTaxi : roboTaxisWithinMaxPickup.values()) {
            List<Directive> currentMenu = roboTaxi.getUnmodifiableViewOfCourses();
            for (int i = 0; i < currentMenu.size(); i++) {
                for (int j = i + 1; j < currentMenu.size() + 1; j++) {
                    GlobalAssert.that(i < j);
                    List<Directive> newMenu = new ArrayList<>(currentMenu);
                    newMenu.add(i, pickupCourse);
                    newMenu.add(j, dropoffCourse);
                    SharedAvRoute sharedAvRoute = SharedAvRoute.of(newMenu, roboTaxi.getDivertableLocation(), now, pickupDuration, dropoffDuration, timeDb);
                    avRouteHandler.add(roboTaxi, sharedAvRoute);
                }
            }
            oldRoutes.put(roboTaxi, SharedAvRoute.of(roboTaxi.getUnmodifiableViewOfCourses(), roboTaxi.getDivertableLocation(), now, pickupDuration, dropoffDuration, timeDb));
        }
        Optional<Entry<RoboTaxi, List<Directive>>> rt = getFastestValidEntry(avRouteHandler, avRequest, oldRoutes, now, requestMaintainer);
        rt.ifPresent(rtle -> GlobalAssert.that(Compatibility.of(rtle.getValue()).forCapacity(rtle.getKey().getScheduleManager(), rtle.getKey().getCapacity())));
        return rt;
    }

    private Optional<Entry<RoboTaxi, List<Directive>>> getFastestValidEntry(AvRouteHandler avRouteHandler, PassengerRequest avRequest, Map<RoboTaxi, SharedAvRoute> oldRoutes,
            double now, RequestHandler requestMaintainer) {
        int numberEntries = avRouteHandler.getNumbervalues();
        for (int i = 0; i < numberEntries; i++) {
            GlobalAssert.that(!avRouteHandler.isEmpty());
            double nextValue = avRouteHandler.getNextvalue();
            Map<RoboTaxi, Set<SharedAvRoute>> map = avRouteHandler.getCopyOfNext();
            for (Entry<RoboTaxi, Set<SharedAvRoute>> entry : map.entrySet())
                for (SharedAvRoute sharedAvRoute : entry.getValue())
                    if (isValidRoute(sharedAvRoute, oldRoutes.get(entry.getKey()), requestMaintainer.getRequestWrap(avRequest), now, requestMaintainer))
                        if (Compatibility.of(sharedAvRoute.getRoboTaxiMenu()).forCapacity(entry.getKey().getScheduleManager(), entry.getKey().getCapacity()))
                            return Optional.of(new SimpleEntry<>(entry.getKey(), sharedAvRoute.getRoboTaxiMenu()));

            avRouteHandler.remove(nextValue);
            GlobalAssert.that(!avRouteHandler.contains(nextValue));
        }
        return Optional.empty();
    }

    public boolean menuFulfillsConstraints( //
            RoboTaxi roboTaxi, List<Directive> newRoute, //
            PassengerRequest avRequest, double now, //
            CachedNetworkTimeDistance timeDb, RequestHandler requestMaintainer) {
        Set<PassengerRequest> uniqueRequests = new HashSet<>();

        for (Directive directive : roboTaxi.getScheduleManager().getDirectives()) {
            if (directive instanceof StopDirective) {
                uniqueRequests.add(((StopDirective) directive).getRequest());
            }
        }

        Set<PassengerRequest> newRouteRequests = new HashSet<>();

        for (Directive directive : newRoute) {
            if (directive instanceof StopDirective) {
                newRouteRequests.add(((StopDirective) directive).getRequest());
            }
        }

        Set<PassengerRequest> currentRequests = uniqueRequests;
        GlobalAssert.that(newRouteRequests.containsAll(currentRequests));
        SharedAvRoute sharedAvRoute = SharedAvRoute.of(newRoute, roboTaxi.getDivertableLocation(), now, pickupDuration, dropoffDuration, timeDb);
        SharedAvRoute oldRoute = SharedAvRoute.of(roboTaxi.getUnmodifiableViewOfCourses(), roboTaxi.getDivertableLocation(), now, pickupDuration, dropoffDuration, timeDb);
        return isValidRoute(sharedAvRoute, oldRoute, requestMaintainer.getRequestWrap(avRequest), now, requestMaintainer);
    }
}
