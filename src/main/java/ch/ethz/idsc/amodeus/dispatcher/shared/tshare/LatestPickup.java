/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum LatestPickup {
    ;

    /** @return {@link Scalar} maximum pickup delay, which is the
     *         submission time calculated from the {@link AVRequest} @param request and
     *         a constant {@link Scalar} value @param maxPickupDelay */
    public static Scalar of(AVRequest request, Scalar maxPickupDelay) {
        return Quantity.of(request.getSubmissionTime(), SI.SECOND).add(maxPickupDelay);
    }

    /** @return {@link Scalar} time left to pick up the {@link AVRequest} @param request with
     *         the {@link Scalar} @param maxPickupDelay given the current time @param now, returns zero
     *         if already overdue. */
    public static Scalar timeTo(AVRequest request, Scalar maxPickupDelay, double now) {
        Scalar latestPickup = Quantity.of(request.getSubmissionTime(), SI.SECOND).add(maxPickupDelay);
        Scalar time = Quantity.of(now, SI.SECOND);
        Scalar slack = latestPickup.subtract(time);
        Scalar timeTo = Scalars.lessThan(slack, Quantity.of(0, SI.SECOND)) ? //
                Quantity.of(0, "s") : slack;
        return timeTo;
    }
}
