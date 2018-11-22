package ch.ethz.idsc.amodeus.dispatcher.shared.kockelman;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.matsim.av.passenger.AVRequest;

/*package*/ class RideSharingConstraints {

    private final double dropoffDuration;
    private final double maxPickupTime;
    private final double maxDriveTimeIncrease;
    private final double maxRemainingTimeIncreas;
    private final double newTravelerMinIncreaseAllowed;

    /* package */ RideSharingConstraints(double maxPickupTime, double maxDriveTimeIncrease, double maxRemainingTimeIncreas, double dropoffDuration,
            double newTravelerMinIncreaseAllowed) {
        this.maxPickupTime = maxPickupTime;
        this.maxDriveTimeIncrease = maxDriveTimeIncrease;
        this.maxRemainingTimeIncreas = maxRemainingTimeIncreas;
        this.dropoffDuration = dropoffDuration;
        this.newTravelerMinIncreaseAllowed = newTravelerMinIncreaseAllowed;
    }

    /** Constraint 1
     * Checks for each request of the old route if the drive times of the new Route are larger than the unit capacity drive time times the maximal allowed increase
     * 
     * @param driveTimes
     * @param newAvRequest
     * @param requestMaintainer
     * @return */
    /* package */ boolean driveTimeCurrentPassengersExceeded(Map<AVRequest, Double> driveTimes, AVRequest newAvRequest, RequestMaintainer requestMaintainer) {
        for (AVRequest avRequest : driveTimes.keySet()) {
            if (!avRequest.equals(newAvRequest)) {
                if (driveTimes.get(avRequest) > maxDriveTimeIncrease * requestMaintainer.getDriveTimeDirectUnitCap(avRequest)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* package */ boolean remainingTimeCurrentPassengerExceeded(SharedAvRoute sharedAvRoute, SharedAvRoute oldRoute, double now) {
        Map<AVRequest, Double> newrouteRemainingTimes = getRemainingTimes(sharedAvRoute, now);
        Map<AVRequest, Double> oldrouteRemainingTimes = getRemainingTimes(oldRoute, now);
        for (Entry<AVRequest, Double> entry : oldrouteRemainingTimes.entrySet()) {
            if (newrouteRemainingTimes.get(entry.getKey()) > entry.getValue() * maxRemainingTimeIncreas) {
                return true;
            }
        }
        return false;
    }

    /* package */ boolean driveTimeNewPassengerExceeded(double newDriveTime, double unitCapacityDriveTime) {
        double maxDriveTimeNewRequest = Math.max(maxDriveTimeIncrease * unitCapacityDriveTime, unitCapacityDriveTime + newTravelerMinIncreaseAllowed);
        return newDriveTime > maxDriveTimeNewRequest;
    }

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
