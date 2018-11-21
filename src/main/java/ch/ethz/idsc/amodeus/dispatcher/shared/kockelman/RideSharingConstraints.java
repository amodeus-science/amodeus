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

    /* package */ RideSharingConstraints(double maxPickupTime, double maxDriveTimeIncrease, double maxRemainingTimeIncreas, double dropoffTime,
            double newTravelerMinIncreaseAllowed) {
        this.maxPickupTime = maxPickupTime;
        this.maxDriveTimeIncrease = maxDriveTimeIncrease;
        this.maxRemainingTimeIncreas = maxRemainingTimeIncreas;
        this.dropoffDuration = dropoffTime;
        this.newTravelerMinIncreaseAllowed = newTravelerMinIncreaseAllowed;
    }

    /* package */ boolean constraint1(Map<AVRequest, Double> driveTimes, AVRequest newAvRequest, RequestMaintainer requestMaintainer) {
        for (AVRequest avRequest : driveTimes.keySet()) {
            if (!avRequest.equals(newAvRequest)) {
                if (driveTimes.get(avRequest) > maxDriveTimeIncrease * requestMaintainer.getDriveTimeDirectUnitCap(avRequest)) {
                    return false;
                }
            }
        }
        return true;
    }

    /* package */ boolean constraint2(SharedAvRoute sharedAvRoute, SharedAvRoute oldRoute, double now) {
        Map<AVRequest, Double> newrouteRemainingTimes = getRemainingTimes(sharedAvRoute, now);
        Map<AVRequest, Double> oldrouteRemainingTimes = getRemainingTimes(oldRoute, now);
        for (Entry<AVRequest, Double> entry : oldrouteRemainingTimes.entrySet()) {
            if (newrouteRemainingTimes.get(entry.getKey()) > entry.getValue() * maxRemainingTimeIncreas) {
                return false;
            }
        }
        return true;
    }

    /* package */ boolean constraint3(Map<AVRequest, Double> driveTimes, AVRequest newAvRequest, double unitCapacityDriveTime) {
        double maxDriveTimeNewRequest = Math.max(maxDriveTimeIncrease * unitCapacityDriveTime, unitCapacityDriveTime + newTravelerMinIncreaseAllowed);
        return driveTimes.get(newAvRequest) > maxDriveTimeNewRequest;
    }

    /* package */ boolean constraint4(SharedAvRoute sharedAvRoute, double now) {
        for (SharedRoutePoint sharedRoutePoint : sharedAvRoute.getRoute()) {
            if (sharedRoutePoint.getMealType().equals(SharedMealType.PICKUP)) {
                if (sharedRoutePoint.getArrivalTime() >= now + maxPickupTime) {
                    return false;
                }
            }
        }
        return false;
    }

    /* package */ boolean constraint5(SharedAvRoute sharedAvRoute, SharedAvRoute oldRoute, Double unitCapacityDriveTime) {
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
