package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum LatestPickup {
    ;

    public static Scalar of(AVRequest avr, Scalar pickupDelayMax) {
        return Quantity.of(avr.getSubmissionTime(), "s").add(pickupDelayMax);
    }

}
