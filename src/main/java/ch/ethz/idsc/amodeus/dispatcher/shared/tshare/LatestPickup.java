/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum LatestPickup {
    ;

    /** @param avRequest
     * @param pickupDelayMax
     * @return */
    public static Scalar of(AVRequest avRequest, Scalar pickupDelayMax) {
        return Quantity.of(avRequest.getSubmissionTime(), SI.SECOND).add(pickupDelayMax);
    }

}
