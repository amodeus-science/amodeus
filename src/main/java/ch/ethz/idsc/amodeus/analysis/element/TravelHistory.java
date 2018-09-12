/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.qty.Quantity;

/** TODO currently only the last and successful pickup attempt is recorded.
 * Additionally, it is possible to record also all pickup attempts in a
 * {@link RequestStatus} history similar to: {REQUESTED,REQUESTED,
 * ASSIGNED,PICKUPDRIVE,PICKUPDRIVE,REQUESTED,ASSIGNED,PICKUPDRIVE,
 * PICKUP,DRIVE,DRIVE,DROPOFF} */

public class TravelHistory {
    private final Scalar defaultValue = Quantity.of(-1, SI.SECOND);
    public final int reqIndx;
    public final int fromLinkIndx;
    public final int toLinkIndx;
    public final Scalar submsnTime;
    private Scalar asgnmtTime = defaultValue;
    /** the pickup process, typically 10[s], is not counted as waiting */
    private Scalar waitEndTme = defaultValue;
    private Scalar pickupTime = defaultValue;
    private Scalar drpOffTime = defaultValue;

    // --
    private Scalar timePrev = defaultValue;

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
            waitEndTme = timePrev;
            pickupTime = now;
            break;
        case DROPOFF:
            GlobalAssert.that(Scalars.lessEquals(waitEndTme, now));
            if (asgnmtTime.equals(defaultValue)) {
                asgnmtTime = now.subtract(Quantity.of(2, SI.SECOND));
            }
            if (waitEndTme == defaultValue) {
                waitEndTme = now.subtract(Quantity.of(1, SI.SECOND));
            }
            drpOffTime = now;
            break;
        default:
            break;
        }
        timePrev = now;
    }

    public Scalar getTotalTravelTime(Scalar tLast) {
        if (drpOffTime.equals(defaultValue))
            return tLast.subtract(submsnTime);
        Scalar totalTravelTime = drpOffTime.subtract(submsnTime);
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), totalTravelTime));
        return totalTravelTime;
    }

    public Scalar getDriveTime(Scalar tLast) {
        if (drpOffTime.equals(defaultValue) && !waitEndTme.equals(defaultValue))
            return tLast.subtract(waitEndTme);
        Scalar driveTime = drpOffTime.subtract(waitEndTme);
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), driveTime));
        return driveTime;
    }

    public Scalar getWaitTime(Scalar tLast) {
        if (waitEndTme.equals(defaultValue))
            return tLast.subtract(submsnTime);
        Scalar waitTime = waitEndTme.subtract(submsnTime);
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), waitTime));
        return waitTime;
    }

    public Scalar getAssignmentTime() {
        return asgnmtTime;
    }

    public Scalar getDropOffTime() {
        return drpOffTime;
    }

    public Scalar getWaitEndTime() {
        return waitEndTme;
    }

    public void isConsistent() {
        if (!drpOffTime.equals(defaultValue)) {
            /** default value used for convenience as this is only called when tLast is not used in
             * any of the three functions. */
            GlobalAssert.that(getTotalTravelTime(defaultValue)//
                    .equals(getWaitTime(defaultValue).add(getDriveTime(defaultValue))));
        }
    }
}
