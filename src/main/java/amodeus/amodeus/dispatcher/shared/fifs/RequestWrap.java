/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.fifs;

import java.util.Optional;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.util.math.GlobalAssert;

/** A {@link RequestWrap} contains more Information about the Request. for example the drive time in the unit capacity case */
/* package */ class RequestWrap {
    private final PassengerRequest avRequest;
    private Optional<Double> pickupTime = Optional.empty();
    private Optional<Double> unitCapacityDriveTime = Optional.empty();
    private boolean isOnWaitList = false;
    private boolean isOnExtreemWaitList = false;

    public RequestWrap(PassengerRequest avRequest) {
        this.avRequest = avRequest;
    }

    public PassengerRequest getAvRequest() {
        return avRequest;
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
        return pickupTime.orElseThrow(RuntimeException::new);
    }

    public void setUnitCapDriveTime(double doubleValue) {
        unitCapacityDriveTime = Optional.of(doubleValue); // is null valid? -> ofNullable
    }

    public double getUnitDriveTime() {
        return unitCapacityDriveTime.orElseThrow(RuntimeException::new);
    }
}
