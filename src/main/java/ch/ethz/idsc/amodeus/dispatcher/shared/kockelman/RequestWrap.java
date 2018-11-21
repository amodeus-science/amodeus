/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.kockelman;

import java.util.Optional;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class RequestWrap {
    private final AVRequest avRequest;
    // private Optional<Double> assignementTime = Optional.empty();
    private Optional<Double> pickupTime = Optional.empty();
    private Optional<Double> unitCapacityDriveTime = Optional.empty();
    private boolean isOnWaitList = false;
    private boolean isOnExtreemWaitList = false;

    public RequestWrap(AVRequest avRequest) {
        this.avRequest = avRequest;
    }

    public AVRequest getAvRequest() {
        return avRequest;
    }

    public double getSubmissionTime() {
        return avRequest.getSubmissionTime();
    }

    public void putToWaitList() {
        isOnWaitList = true;
    }

    public void putToExtreemWaitList() {
        isOnExtreemWaitList = true;
    }

    public boolean isOnWaitList() {
        return isOnWaitList;
    }

    public boolean isOnExtreemWaitList() {
        return isOnExtreemWaitList;
    }

    public void setPickupTime(double now) {
        GlobalAssert.that(!pickupTime.isPresent()); // The Pickup Time Can only be set Once
        pickupTime = Optional.of(now);
    }

    public double getPickupTime() {
        GlobalAssert.that(pickupTime.isPresent());
        return pickupTime.get();
    }

    public void setUnitCapDriveTime(double doubleValue) {
        unitCapacityDriveTime = Optional.of(doubleValue);
    }

    public double getUnitDriveTime() {
        GlobalAssert.that(unitCapacityDriveTime.isPresent());
        return unitCapacityDriveTime.get();
    }

}
