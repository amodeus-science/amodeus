package ch.ethz.idsc.amodeus.dispatcher.shared;

import org.matsim.api.core.v01.Id;

import ch.ethz.matsim.av.passenger.AVRequest;

public class SharedAVCourse {

    private final Id<AVRequest> requestId;
    private final SharedAVMealType pickupOrDropOff;

    SharedAVCourse(Id<AVRequest> requestId, SharedAVMealType pickupOrDropOff) {
        this.requestId = requestId;
        this.pickupOrDropOff = pickupOrDropOff;
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

    public Id<AVRequest> getRequestId() {
        return requestId;
    }

}
