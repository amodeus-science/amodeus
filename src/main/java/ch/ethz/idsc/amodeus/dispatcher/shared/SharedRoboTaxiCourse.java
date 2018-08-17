/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.Objects;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Request;

/** Middle level class in SharedRoboTaxi functionality, a {@link SharedRoboTaxiMenu} is
 * composed of {@link SharedRoboTaxiCourse}s which internally have a {@link SharedRoboTaxiMealType}s */
public class SharedRoboTaxiCourse {
    public static SharedRoboTaxiCourse pickupCourse(Id<Request> requestId) {
        return new SharedRoboTaxiCourse(requestId, SharedRoboTaxiMealType.PICKUP);
    }

    public static SharedRoboTaxiCourse dropoffCourse(Id<Request> requestId) {
        return new SharedRoboTaxiCourse(requestId, SharedRoboTaxiMealType.DROPOFF);
    }

    // ---
    private final Id<Request> requestId;
    private final SharedRoboTaxiMealType sharedAVMealType;

    public SharedRoboTaxiCourse(Id<Request> requestId, SharedRoboTaxiMealType sharedAVMealType) {
        this.requestId = requestId;
        this.sharedAVMealType = sharedAVMealType;
    }

    public SharedRoboTaxiMealType getPickupOrDropOff() {
        return sharedAVMealType;
    }

    public Id<Request> getRequestId() {
        return requestId;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SharedRoboTaxiCourse) {
            SharedRoboTaxiCourse sharedAVCourse = (SharedRoboTaxiCourse) object;
            return sharedAVCourse.getRequestId().equals(requestId) && //
                    sharedAVCourse.getPickupOrDropOff().equals(sharedAVMealType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId.toString(), sharedAVMealType);
    }
}
