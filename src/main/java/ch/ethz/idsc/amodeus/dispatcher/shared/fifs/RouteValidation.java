/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiUtils;
import ch.ethz.idsc.amodeus.dispatcher.shared.Compatibility;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
import ch.ethz.idsc.amodeus.routing.TravelTimeComputationCached;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class RouteValidation {

    private final double pickupDuration;
    private final double dropoffDuration;
    private final double maxPickupTime;

    private final RideSharingConstraints rideSharingConstraints;

    RouteValidation(double maxPickupTime, double maxDriveTimeIncrease, double maxRemainingTimeIncreas, double dropoffTime, double pickupDuration,
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
    boolean isValidRoute(SharedAvRoute sharedAvRoute, SharedAvRoute oldRoute, RequestWrap newRequestWrap, double now, RequestHandler requestHandler) {
        Map<AVRequest, Double> driveTimes = requestHandler.getDriveTimes(sharedAvRoute);
        AVRequest newAvRequest = newRequestWrap.getAvRequest();
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

    /** @param allRoboTaxis
     * @param robotaxisWithMenu
     * @param avRequest
     * @param now
     * @param timeDb
     * @param roboTaxiMaintainer
     * @param maxTime
     * @param timeSharing
     * @return The Closest RoboTaxi with a Shared Menu associated with it. */
    /* package */ Optional<Entry<RoboTaxi, List<SharedCourse>>> getClosestValidSharingRoboTaxi(Set<RoboTaxi> robotaxisWithMenu, AVRequest avRequest, double now,
            TravelTimeComputationCached timeDb, RequestHandler requestMaintainer, RoboTaxiHandler roboTaxiMaintainer, double maxTime) {

        GlobalAssert.that(robotaxisWithMenu.stream().allMatch(r -> RoboTaxiUtils.hasNextCourse(r)));

        NavigableMap<Double, RoboTaxi> robotaxisWithinMaxPickup = RoboTaxiUtilsFagnant.getRoboTaxisWithinMaxTime(avRequest.getFromLink(), robotaxisWithMenu, timeDb, maxPickupTime,
                roboTaxiMaintainer);

        AvRouteHandler avRouteHandler = new AvRouteHandler();
        SharedCourse pickupCourse = SharedCourse.pickupCourse(avRequest);
        SharedCourse dropoffCourse = SharedCourse.dropoffCourse(avRequest);
        Map<RoboTaxi, SharedAvRoute> oldRoutes = new HashMap<>();

        // Calculate all routes and times
        for (RoboTaxi roboTaxi : robotaxisWithinMaxPickup.values()) {
            List<SharedCourse> currentMenu = roboTaxi.getUnmodifiableViewOfCourses();
            for (int i = 0; i < currentMenu.size(); i++) {
                for (int j = i + 1; j < currentMenu.size() + 1; j++) {
                    GlobalAssert.that(i < j);
                    List<SharedCourse> newMenu = new ArrayList<>(currentMenu);
                    newMenu.add(i, pickupCourse);
                    newMenu.add(j, dropoffCourse);
                    SharedAvRoute sharedAvRoute = SharedAvRoute.of(newMenu, roboTaxi.getDivertableLocation(), now, pickupDuration, dropoffDuration, timeDb);
                    avRouteHandler.add(roboTaxi, sharedAvRoute);
                }
            }
            oldRoutes.put(roboTaxi, SharedAvRoute.of(roboTaxi.getUnmodifiableViewOfCourses(), roboTaxi.getDivertableLocation(), now, pickupDuration, dropoffDuration, timeDb));
        }
        Optional<Entry<RoboTaxi, List<SharedCourse>>> rt = getFastestValidEntry(avRouteHandler, avRequest, oldRoutes, now, requestMaintainer);
        if (rt.isPresent()) {
            GlobalAssert.that(Compatibility.of(rt.get().getValue()).forCapacity(rt.get().getKey().getCapacity()));
        }
        return rt;
    }

    private Optional<Entry<RoboTaxi, List<SharedCourse>>> getFastestValidEntry(AvRouteHandler avRouteHandler, AVRequest avRequest, Map<RoboTaxi, SharedAvRoute> oldRoutes,
            double now, RequestHandler requestMaintainer) {
        int numberEntries = avRouteHandler.getNumbervalues();
        for (int i = 0; i < numberEntries; i++) {
            GlobalAssert.that(!avRouteHandler.isEmpty());
            double nextValue = avRouteHandler.getNextvalue();
            Map<RoboTaxi, Set<SharedAvRoute>> map = avRouteHandler.getCopyOfNext();
            for (Entry<RoboTaxi, Set<SharedAvRoute>> entry : map.entrySet())
                for (SharedAvRoute sharedAvRoute : entry.getValue())
                    if (isValidRoute(sharedAvRoute, oldRoutes.get(entry.getKey()), requestMaintainer.getRequestWrap(avRequest), now, requestMaintainer))
                        if (Compatibility.of(sharedAvRoute.getRoboTaxiMenu()).forCapacity(entry.getKey().getCapacity())) {
                            return Optional.of(new SimpleEntry<>(entry.getKey(), sharedAvRoute.getRoboTaxiMenu()));
                        }

            avRouteHandler.remove(nextValue);
            GlobalAssert.that(!avRouteHandler.contains(nextValue));
        }
        return Optional.empty();
    }

    boolean menuFulfillsConstraints( //
            RoboTaxi roboTaxi, List<SharedCourse> newRoute, //
            AVRequest avRequest, double now, //
            TravelTimeComputationCached timeDb, RequestHandler requestMaintainer) {
        Set<AVRequest> currentRequests = RoboTaxiUtils.getRequestsInMenu(roboTaxi);
        GlobalAssert.that(SharedCourseListUtils.getUniqueAVRequests(newRoute).containsAll(currentRequests));
        SharedAvRoute sharedAvRoute = SharedAvRoute.of(newRoute, roboTaxi.getDivertableLocation(), now, pickupDuration, dropoffDuration, timeDb);
        SharedAvRoute oldRoute = SharedAvRoute.of(roboTaxi.getUnmodifiableViewOfCourses(), roboTaxi.getDivertableLocation(), now, pickupDuration, dropoffDuration, timeDb);
        return isValidRoute(sharedAvRoute, oldRoute, requestMaintainer.getRequestWrap(avRequest), now, requestMaintainer);
    }

}
