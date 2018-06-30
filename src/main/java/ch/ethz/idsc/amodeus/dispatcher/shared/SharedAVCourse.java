package ch.ethz.idsc.amodeus.dispatcher.shared;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Request;

public class SharedAVCourse {

    private final Id<Request> requestId;
    private final SharedAVMealType pickupOrDropOff;

    public SharedAVCourse(Id<Request> requestId, SharedAVMealType pickupOrDropOff) {
        this.requestId = requestId;
        this.pickupOrDropOff = pickupOrDropOff;
    }

    public SharedAVCourse(SharedAVCourse sharedAVCourse) {
        this(sharedAVCourse.requestId, sharedAVCourse.pickupOrDropOff);
    }

    public static SharedAVCourse pickupCourse(Id<Request> requestId) {
        return new SharedAVCourse(requestId, SharedAVMealType.PICKUP);
    }

    public static SharedAVCourse dropoffCourse(Id<Request> requestId) {
        return new SharedAVCourse(requestId, SharedAVMealType.DROPOFF);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof SharedAVCourse) {
            SharedAVCourse course = (SharedAVCourse) obj;
            return course.getRequestId() == requestId && course.getPickupOrDropOff().equals(pickupOrDropOff);
        }
        return false;
    }

    public SharedAVMealType getPickupOrDropOff() {
        return pickupOrDropOff;
    }

    public Id<Request> getRequestId() {
        return requestId;
    }

    public SharedAVCourse copy() {
        return new SharedAVCourse(this);
    }

}
