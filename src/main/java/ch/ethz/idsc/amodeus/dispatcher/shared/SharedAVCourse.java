/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.Objects;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Request;

/** class is immutable that means there is no need to make a copy of
 * an instance, but instead the instance can be shared and reused.
 * 
 * @author Nicolo Ormezzano, Lukas Sieber */
public class SharedAVCourse {
    public static SharedAVCourse pickupCourse(Id<Request> requestId) {
        return new SharedAVCourse(requestId, SharedAVMealType.PICKUP);
    }

    public static SharedAVCourse dropoffCourse(Id<Request> requestId) {
        return new SharedAVCourse(requestId, SharedAVMealType.DROPOFF);
    }

    // ---
    private final Id<Request> requestId;
    private final SharedAVMealType sharedAVMealType;

    public SharedAVCourse(Id<Request> requestId, SharedAVMealType sharedAVMealType) {
        this.requestId = requestId;
        this.sharedAVMealType = sharedAVMealType;
    }

    public SharedAVMealType getPickupOrDropOff() {
        return sharedAVMealType;
    }

    public Id<Request> getRequestId() {
        return requestId;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SharedAVCourse) {
            SharedAVCourse sharedAVCourse = (SharedAVCourse) object;
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
