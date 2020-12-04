/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.fifs;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

/** In this class the constraints of ride sharing are defined. */
/* package */ class RideSharingConstraints {

    private final double dropoffDuration;
    private final double maxPickupTime;
    private final double maxDriveTimeIncrease;
    private final double maxRemainingTimeIncrease;
    private final double newTravelerMinIncreaseAllowed;

    public RideSharingConstraints(double maxPickupTime, double maxDriveTimeIncrease, double maxRemainingTimeIncreas, double dropoffDuration, double newTravelerMinIncreaseAllowed) {
        this.maxPickupTime = maxPickupTime;
        this.maxDriveTimeIncrease = maxDriveTimeIncrease;
        this.maxRemainingTimeIncrease = maxRemainingTimeIncreas;
        this.dropoffDuration = dropoffDuration;
        this.newTravelerMinIncreaseAllowed = newTravelerMinIncreaseAllowed;
    }

    /** Constraint 1
     * Current passengers’ trip duration increases ≤ 20% (total trip duration
     * with ride-sharing vs. without ride-sharing);
     *
     * Checks for each request of the old route if the drive times of the new Route
     * are larger than the unit capacity drive time times the maximal allowed increase
     * 
     * @param driveTimes
     * @param newAvRequest
     * @param requestMaintainer
     * @return */
    public boolean driveTimeCurrentPassengersExceeded(Map<PassengerRequest, Double> driveTimes, PassengerRequest newAvRequest, RequestHandler requestMaintainer) {
        for (PassengerRequest avRequest : driveTimes.keySet())
            if (!avRequest.equals(newAvRequest))
                if (driveTimes.get(avRequest) > maxDriveTimeIncrease * requestMaintainer.getDriveTimeDirectUnitCap(avRequest))
                    return true;
        return false;
    }

    /** Constraint 2
     * Current passengers’ remaining trip time increases ≤ 40%;
     * 
     * Checks for each request in the old route if the remaining time in the new route
     * is increased to the current remaining time by more than 40%
     * 
     * @param sharedAvRoute
     * @param oldRoute
     * @param now
     * @return true if the remaining time of one passenger in the new route was more than maxRemainingTimeIncrease larger than its current remaining time. Thus
     *         if
     *         true this new route is not a valid possibility. returns false if the second constraint is satisfied for all requests. */
    public boolean remainingTimeCurrentPassengerExceeded(SharedAvRoute sharedAvRoute, SharedAvRoute oldRoute, double now) {
        Map<PassengerRequest, Double> newrouteRemainingTimes = getRemainingTimes(sharedAvRoute, now);
        Map<PassengerRequest, Double> oldrouteRemainingTimes = getRemainingTimes(oldRoute, now);
        for (Entry<PassengerRequest, Double> entry : oldrouteRemainingTimes.entrySet())
            if (newrouteRemainingTimes.get(entry.getKey()) > entry.getValue() * maxRemainingTimeIncrease)
                return true;
        return false;
    }

    /** Constraint 3
     * New traveler’s total trip time increase grows by ≤ Max(20% total trip without ride-sharing, or 3 minutes);
     * 
     * @param newDriveTime
     * @param unitCapacityDriveTime
     * @return */
    public boolean driveTimeNewPassengerExceeded(double newDriveTime, double unitCapacityDriveTime) {
        double maxDriveTimeNewRequest = Math.max(maxDriveTimeIncrease * unitCapacityDriveTime, unitCapacityDriveTime + newTravelerMinIncreaseAllowed);
        return newDriveTime > maxDriveTimeNewRequest;
    }

    /** Constraint 4
     * New travelers will be picked up at least within the next 5 minutes;
     *
     * @param sharedAvRoute
     * @param now
     * @return */
    public boolean waitTimesExceeded(SharedAvRoute sharedAvRoute, double now) {
        for (SharedRoutePoint sharedRoutePoint : sharedAvRoute.getRoute())
            if (sharedRoutePoint.isPickup())
                // TODO @ChengQi Check this constraint could be that from submission to this time
                if (sharedRoutePoint.getArrivalTime() >= now + maxPickupTime)
                    return true;
        return false;
    }

    /** Constraint 5
     * Total planned trip time to serve all passengers ≤ remaining time to serve
     * the current trips + time to serve the new trip + drop-off time, if not pooled.
     *
     * 
     * @param sharedAvRoute
     * @param oldRoute
     * @param unitCapacityDriveTime
     * @return */
    public boolean combinedConstraintAcceptable(SharedAvRoute sharedAvRoute, SharedAvRoute oldRoute, Double unitCapacityDriveTime) {
        return sharedAvRoute.getEndTime() <= oldRoute.getEndTime() + unitCapacityDriveTime + dropoffDuration;
    }

    private static Map<PassengerRequest, Double> getRemainingTimes(SharedAvRoute route, double now) {
        Map<PassengerRequest, Double> remainingTimes = new HashMap<>();
        for (SharedRoutePoint sharedRoutePoint : route.getRoute())
            if (sharedRoutePoint.isDropoff())
                remainingTimes.put(sharedRoutePoint.getRequest(), sharedRoutePoint.getArrivalTime() - now);
        return remainingTimes;
    }
}
