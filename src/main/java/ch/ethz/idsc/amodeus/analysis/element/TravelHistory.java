/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.Objects;

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
    public final int reqIndx;
    public final int fromLinkIndx;
    public final int toLinkIndx;
    public final Scalar submsnTime;
    private Scalar asgnmtTime;
    /** the pickup process, typically 10[s], is not counted as waiting */
    private Scalar waitEndTme;
    private Scalar pickupTime;
    private Scalar drpOffTime;

    // --
    private Scalar timePrev = Quantity.of(0, "s");

    public TravelHistory(RequestContainer requestContainer, long now) {
        fromLinkIndx = requestContainer.fromLinkIndex;
        toLinkIndx = requestContainer.toLinkIndex;
        reqIndx = requestContainer.requestIndex;
        submsnTime = Quantity.of(requestContainer.submissionTime, SI.SECOND);
        if(!(requestContainer.requestStatus.contains(RequestStatus.REQUESTED))){
            requestContainer.requestStatus.stream().forEach(rs->{
                System.err.println(rs);
            });
        }
        GlobalAssert.that(requestContainer.requestStatus.contains(RequestStatus.REQUESTED));
        register(requestContainer, Quantity.of(now, SI.SECOND));
    }

    public void register(RequestContainer requestContainer, Scalar now) {
        if (requestContainer.requestStatus.contains(RequestStatus.ASSIGNED))
            asgnmtTime = now;
        if (requestContainer.requestStatus.contains(RequestStatus.PICKUP)) {
            waitEndTme = timePrev;
            pickupTime = now;
        }
        if (requestContainer.requestStatus.contains(RequestStatus.DROPOFF)) {
            drpOffTime = now;
        }
        timePrev = now;
    }

    public Scalar getTotalTravelTime(Scalar tLast) {
        Objects.requireNonNull(submsnTime);
        Objects.requireNonNull(drpOffTime);
        Scalar totalTravelTime = drpOffTime.subtract(submsnTime);
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), totalTravelTime));
        return totalTravelTime;
    }

    public Scalar getDriveTime(Scalar tLast) {
        Objects.requireNonNull(waitEndTme);
        Objects.requireNonNull(drpOffTime);
        Scalar driveTime = drpOffTime.subtract(waitEndTme);
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), driveTime));
        return driveTime;
    }

    public Scalar getWaitTime(Scalar tLast) {
        Objects.requireNonNull(waitEndTme);
        Objects.requireNonNull(submsnTime);
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

}
