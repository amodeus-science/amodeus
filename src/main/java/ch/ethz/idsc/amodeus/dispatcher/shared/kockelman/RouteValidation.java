/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.kockelman;

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
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/*package*/ class RouteValidation {

    public long calculationTime = 0;

    private final double pickupDuration;
    private final double dropoffDuration;
    private final double maxPickupTime;

    private final RideSharingConstraints rideSharingConstraints;

    /* package */ RouteValidation(double maxPickupTime, double maxDriveTimeIncrease, double maxRemainingTimeIncreas, double dropoffTime, double pickupDuration,
            double newTravelerMinIncreaseAllowed) {
        this.maxPickupTime = maxPickupTime;
        this.dropoffDuration = dropoffTime;
        this.pickupDuration = pickupDuration;
        rideSharingConstraints = new RideSharingConstraints(maxPickupTime, maxDriveTimeIncrease, maxRemainingTimeIncreas, dropoffTime, newTravelerMinIncreaseAllowed);
    }

    /* package */ boolean isValidRoute(SharedAvRoute sharedAvRoute, SharedAvRoute oldRoute, RequestWrap newRequestWrap, double now, RequestMaintainer requestMaintainer) {
        Map<AVRequest, Double> driveTimes = getDriveTimes(sharedAvRoute, requestMaintainer);
        AVRequest newAvRequest = newRequestWrap.getAvRequest();
        double unitCapacityDriveTime = requestMaintainer.getDriveTimeDirectUnitCap(newAvRequest);
        GlobalAssert.that(unitCapacityDriveTime == newRequestWrap.getUnitDriveTime());
        // Requirement 1 Current Passenger Total Travel Time Increase
        if (!rideSharingConstraints.constraint1(driveTimes, newAvRequest, requestMaintainer))
            return false;

        // Requirement 2 Current Passenger remaining Time Increase
        if (!rideSharingConstraints.constraint2(sharedAvRoute, oldRoute, now))
            return false;

        // Requirement 3 New Passenger Drive Time Increase
        if (!rideSharingConstraints.constraint3(driveTimes, newAvRequest, unitCapacityDriveTime))
            return false;

        // Requirement 4 Pick up within 5 min
        if (!rideSharingConstraints.constraint4(sharedAvRoute, now))
            return false;

        // Requirement 5 combined constraint
        return rideSharingConstraints.constraint5(sharedAvRoute, oldRoute, unitCapacityDriveTime);
    }

    private Map<AVRequest, Double> getDriveTimes(SharedAvRoute route, RequestMaintainer requestMaintainer) {
        // Preparation
        Map<AVRequest, Double> thisPickupTimes = new HashMap<>();
        route.getRoute().stream().filter(srp -> srp.getMealType().equals(SharedMealType.PICKUP)).forEach(srp -> thisPickupTimes.put(srp.getAvRequest(), srp.getArrivalTime()));
        //
        Map<AVRequest, Double> driveTimes = new HashMap<>();
        for (SharedRoutePoint sharedRoutePoint : route.getRoute()) {
            if (sharedRoutePoint.getMealType().equals(SharedMealType.DROPOFF)) {
                if (thisPickupTimes.containsKey(sharedRoutePoint.getAvRequest())) {
                    // TODO does it include the dropoff or not?
                    driveTimes.put(sharedRoutePoint.getAvRequest(), sharedRoutePoint.getEndTime() - thisPickupTimes.get(sharedRoutePoint.getAvRequest()));
                } else {
                    driveTimes.put(sharedRoutePoint.getAvRequest(), sharedRoutePoint.getEndTime() - requestMaintainer.getPickupTime(sharedRoutePoint.getAvRequest()));
                }
            }
        }
        return driveTimes;
    }

    /** @param robotaxisWithMenu
     * @param avRequest
     * @param now
     * @param timeDb
     * @param timeSharing
     * @return The Closest RoboTaxi with a Shared Menu associated with it. */
    /* package */ Optional<Entry<RoboTaxi, List<SharedCourse>>> getClosestValidSharingRoboTaxi(Set<RoboTaxi> robotaxisWithMenu, AVRequest avRequest, double now,
            TravelTimeCalculatorCached timeDb, RequestMaintainer requestMaintainer) {
        Long time2 = System.nanoTime();

        GlobalAssert.that(robotaxisWithMenu.stream().allMatch(r -> RoboTaxiUtils.hasNextCourse(r)));
        NavigableMap<Double, RoboTaxi> robotaxisWithinMaxPickup = RoboTaxiUtilsFagnant.getRoboTaxisWithinMaxTime(avRequest.getFromLink(), robotaxisWithMenu, timeDb, maxPickupTime);
        calculationTime += System.nanoTime() - time2;

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

        return getFastestValidEntry(avRouteHandler, avRequest, oldRoutes, now, requestMaintainer);

    }

    private Optional<Entry<RoboTaxi, List<SharedCourse>>> getFastestValidEntry(AvRouteHandler avRouteHandler, AVRequest avRequest, Map<RoboTaxi, SharedAvRoute> oldRoutes,
            double now, RequestMaintainer requestMaintainer) {
        int numberEntries = avRouteHandler.getNumbervalues();
        for (int i = 0; i < numberEntries; i++) {
            GlobalAssert.that(!avRouteHandler.isEmpty());
            double nextValue = avRouteHandler.getNextvalue();
            Map<RoboTaxi, Set<SharedAvRoute>> map = avRouteHandler.getCopyOfNext();
            for (Entry<RoboTaxi, Set<SharedAvRoute>> entry : map.entrySet()) {
                for (SharedAvRoute sharedAvRoute : entry.getValue()) {
                    if (isValidRoute(sharedAvRoute, oldRoutes.get(entry.getKey()), requestMaintainer.getRequestWrap(avRequest), now, requestMaintainer)) {
                        return Optional.of(new SimpleEntry<>(entry.getKey(), sharedAvRoute.getRoboTaxiMenu()));
                    }
                }
            }
            avRouteHandler.remove(nextValue);
            GlobalAssert.that(!avRouteHandler.contains(nextValue));
        }
        return Optional.ofNullable(null);
    }

    /* package */ boolean menuFulfillsConstraints(RoboTaxi roboTaxi, List<SharedCourse> newRoute, AVRequest avRequest, double now, TravelTimeCalculatorCached timeDb,
            RequestMaintainer requestMaintainer) {
        Set<AVRequest> currentRequests = RoboTaxiUtils.getRequestsInMenu(roboTaxi);
        GlobalAssert.that(SharedCourseListUtils.getUniqueAVRequests(newRoute).containsAll(currentRequests));
        SharedAvRoute sharedAvRoute = SharedAvRoute.of(newRoute, roboTaxi.getDivertableLocation(), now, pickupDuration, dropoffDuration, timeDb);
        SharedAvRoute oldRoute = SharedAvRoute.of(roboTaxi.getUnmodifiableViewOfCourses(), roboTaxi.getDivertableLocation(), now, pickupDuration, dropoffDuration, timeDb);
        return isValidRoute(sharedAvRoute, oldRoute, requestMaintainer.getRequestWrap(avRequest), now, requestMaintainer);
    }

    public void setCalculationTimeTo0() {
        calculationTime = 0;
    }
}
