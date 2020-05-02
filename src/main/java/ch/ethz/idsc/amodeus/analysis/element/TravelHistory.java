/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.Objects;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.Sign;

/** TODO @clruch currently only the last and successful pickup attempt is recorded.
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
    private Scalar drpOffTime;

    public TravelHistory(RequestContainer requestContainer, long now) {
        fromLinkIndx = requestContainer.fromLinkIndex;
        toLinkIndx = requestContainer.toLinkIndex;
        reqIndx = requestContainer.requestIndex;
        submsnTime = Quantity.of(requestContainer.submissionTime, SI.SECOND);
        register(requestContainer, Quantity.of(now, SI.SECOND));
    }

    public void register(RequestContainer requestContainer, Scalar now) {
        if (requestContainer.requestStatus.contains(RequestStatus.ASSIGNED))
            asgnmtTime = now;
        if (requestContainer.requestStatus.contains(RequestStatus.PICKUP))
            waitEndTme = now;
        if (requestContainer.requestStatus.contains(RequestStatus.DROPOFF))
            drpOffTime = now;
    }

    /** This function should be called on the last Timestep of the simulation.
     * It makes sure that all the Times are set properly in case that not all requests have been served.
     * 
     * @param tLast */
    public void fillNotFinishedData(Scalar tLast) {
        Objects.requireNonNull(tLast);
        if (Objects.isNull(asgnmtTime))
            asgnmtTime = tLast;
        if (Objects.isNull(waitEndTme))
            waitEndTme = tLast;
        if (Objects.isNull(drpOffTime))
            drpOffTime = tLast;
    }

    public Scalar getTotalTravelTime() {
        Objects.requireNonNull(submsnTime);
        Objects.requireNonNull(drpOffTime);
        return Sign.requirePositiveOrZero(drpOffTime.subtract(submsnTime));
    }

    public Scalar getDriveTime() {
        Objects.requireNonNull(waitEndTme);
        Objects.requireNonNull(drpOffTime);
        return Sign.requirePositiveOrZero(drpOffTime.subtract(waitEndTme));
    }

    public Scalar getWaitTime() {
        Objects.requireNonNull(waitEndTme);
        Objects.requireNonNull(submsnTime);
        return Sign.requirePositiveOrZero(waitEndTme.subtract(submsnTime));
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
