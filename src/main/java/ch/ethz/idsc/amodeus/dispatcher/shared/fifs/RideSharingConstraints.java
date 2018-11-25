/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.matsim.av.passenger.AVRequest;

/**
 * In this class the constraints of ride sharing are defined. 
 */
/* package */ class RideSharingConstraints {

    private final double dropoffDuration;
    private final double maxPickupTime;
    private final double maxDriveTimeIncrease;
    private final double maxRemainingTimeIncrease;
    private final double newTravelerMinIncreaseAllowed;

    /* package */ RideSharingConstraints(double maxPickupTime, double maxDriveTimeIncrease, double maxRemainingTimeIncreas, double dropoffDuration,
            double newTravelerMinIncreaseAllowed) {
        this.maxPickupTime = maxPickupTime;
        this.maxDriveTimeIncrease = maxDriveTimeIncrease;
        this.maxRemainingTimeIncrease = maxRemainingTimeIncreas;
        this.dropoffDuration = dropoffDuration;
        this.newTravelerMinIncreaseAllowed = newTravelerMinIncreaseAllowed;
    }

    /** Constraint 1
     * Current passengers’ trip duration increases ≤ 20% (total trip duration with ride-sharing vs. without ride-sharing);
     *
     * Checks for each request of the old route if the drive times of the new Route are larger than the unit capacity drive time times the maximal allowed increase
     * 
     * @param driveTimes
     * @param newAvRequest
     * @param requestMaintainer
     * @return */
    /* package */ boolean driveTimeCurrentPassengersExceeded(Map<AVRequest, Double> driveTimes, AVRequest newAvRequest, RequestHandler requestMaintainer) {
        for (AVRequest avRequest : driveTimes.keySet()) {
            if (!avRequest.equals(newAvRequest)) {
                if (driveTimes.get(avRequest) > maxDriveTimeIncrease * requestMaintainer.getDriveTimeDirectUnitCap(avRequest)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Constraint 2
     * Current passengers’ remaining trip time increases ≤ 40%;
     * 
     * Checks for each request in the old route if the remaining time in the new route is increased to the current remaining time by more than 40%
     * 
     * @param sharedAvRoute
     * @param oldRoute
     * @param now
     * @return true if the remaining time of one passenger in the new route was more than maxRemainingTimeIncrease larger than its current remaining time. Thus if
     *         true this new route is not a valid possibility. returns false if the second constraint is satisfied for all requests. */
    /* package */ boolean remainingTimeCurrentPassengerExceeded(SharedAvRoute sharedAvRoute, SharedAvRoute oldRoute, double now) {
        Map<AVRequest, Double> newrouteRemainingTimes = getRemainingTimes(sharedAvRoute, now);
        Map<AVRequest, Double> oldrouteRemainingTimes = getRemainingTimes(oldRoute, now);
        for (Entry<AVRequest, Double> entry : oldrouteRemainingTimes.entrySet()) {
            if (newrouteRemainingTimes.get(entry.getKey()) > entry.getValue() * maxRemainingTimeIncrease) {
                return true;
            }
        }
        return false;
    }

    /** Constraint 3
     * New traveler’s total trip time increase grows by ≤ Max(20% total trip without ride-sharing, or 3 minutes);
     * 
     * @param newDriveTime
     * @param unitCapacityDriveTime
     * @return */
    /* package */ boolean driveTimeNewPassengerExceeded(double newDriveTime, double unitCapacityDriveTime) {
        double maxDriveTimeNewRequest = Math.max(maxDriveTimeIncrease * unitCapacityDriveTime, unitCapacityDriveTime + newTravelerMinIncreaseAllowed);
        return newDriveTime > maxDriveTimeNewRequest;
    }

    /** Constraint 4
     * New travelers will be picked up at least within the next 5 minutes;
     *
     * @param sharedAvRoute
     * @param now
     * @return */
    /* package */ boolean waitTimesExceeded(SharedAvRoute sharedAvRoute, double now) {
        for (SharedRoutePoint sharedRoutePoint : sharedAvRoute.getRoute()) {
            if (sharedRoutePoint.getMealType().equals(SharedMealType.PICKUP)) {
                // TODO Check this constraint could be that from submission to this time
                if (sharedRoutePoint.getArrivalTime() >= now + maxPickupTime) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Constraint 5
     * Total planned trip time to serve all passengers ≤ remaining time to serve the current trips + time to serve the new trip + drop-off time, if
     * not pooled.
     *
     * 
     * @param sharedAvRoute
     * @param oldRoute
     * @param unitCapacityDriveTime
     * @return */
    /* package */ boolean combinedConstraintAcceptable(SharedAvRoute sharedAvRoute, SharedAvRoute oldRoute, Double unitCapacityDriveTime) {
        return sharedAvRoute.getEndTime() <= oldRoute.getEndTime() + unitCapacityDriveTime + dropoffDuration;
    }

    private Map<AVRequest, Double> getRemainingTimes(SharedAvRoute route, double now) {
        Map<AVRequest, Double> remainingTimes = new HashMap<>();
        for (SharedRoutePoint sharedRoutePoint : route.getRoute()) {
            if (sharedRoutePoint.getMealType().equals(SharedMealType.DROPOFF)) {
                remainingTimes.put(sharedRoutePoint.getAvRequest(), sharedRoutePoint.getArrivalTime() - now);
            }
        }
        return remainingTimes;
    }
}
