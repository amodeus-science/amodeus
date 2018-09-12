/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.qty.Quantity;

public class TravelHistory {
    private final Scalar defaultValue = Quantity.of(-1, SI.SECOND);
    public final int reqIndx;
    public final int fromLinkIndx;
    public final int toLinkIndx;
    public final Scalar submsnTime;
    private Scalar asgnmtTime = defaultValue;
    private Scalar pickupTime = defaultValue;
    private Scalar drpOffTime = defaultValue;

    public TravelHistory(RequestContainer requestContainer, long now) {
        fromLinkIndx = requestContainer.fromLinkIndex;
        toLinkIndx = requestContainer.toLinkIndex;
        reqIndx = requestContainer.requestIndex;
        submsnTime = Quantity.of(requestContainer.submissionTime, SI.SECOND);
        GlobalAssert.that(requestContainer.requestStatus.equals(RequestStatus.REQUESTED));
        register(requestContainer, now);
    }

    public void register(RequestContainer requestContainer, long nowL) {
        Scalar now = Quantity.of(nowL, SI.SECOND);
        switch (requestContainer.requestStatus) {
        case REQUESTED:
            break;
        case ASSIGNED:
            GlobalAssert.that(Scalars.lessEquals(submsnTime, now));
            asgnmtTime = now;
            break;
        case PICKUP:
            GlobalAssert.that(Scalars.lessEquals(asgnmtTime, now));
            if (asgnmtTime.equals(defaultValue)) {
                asgnmtTime = now.subtract(Quantity.of(1, SI.SECOND));
            }
            pickupTime = now;
            break;
        case DROPOFF:
            GlobalAssert.that(Scalars.lessEquals(pickupTime, now));
            if (asgnmtTime.equals(defaultValue)) {
                asgnmtTime = now.subtract(Quantity.of(2, SI.SECOND));
            }
            if (pickupTime == defaultValue) {
                pickupTime = now.subtract(Quantity.of(1, SI.SECOND));
            }
            drpOffTime = now;
            break;
        default:
            break;
        }
    }

    public Scalar getTotalTravelTime() {
        if (drpOffTime.equals(defaultValue))
            return defaultValue;
        Scalar totalTravelTime = drpOffTime.subtract(submsnTime);
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), totalTravelTime));
        return totalTravelTime;
    }

    public Scalar getDriveTime() {
        if (drpOffTime.equals(defaultValue) || pickupTime.equals(defaultValue))
            return defaultValue;
        Scalar driveTime = drpOffTime.subtract(pickupTime);
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), driveTime));
        return driveTime;
    }

    public Scalar getWaitTime() {
        if (pickupTime.equals(defaultValue))
            return defaultValue;
        Scalar waitTime = pickupTime.subtract(submsnTime);
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), waitTime));
        return waitTime;
    }

    public Scalar getAssignmentTime() {
        return asgnmtTime;
    }

    public Scalar getDropOffTime() {
        return drpOffTime;
    }

    public Scalar getPickupTime() {
        return pickupTime;
    }

    public void isConsistent() {
        if (!drpOffTime.equals(defaultValue)) {
            GlobalAssert.that(getTotalTravelTime().equals(getWaitTime().add(getDriveTime())));
        }
    }
}
