/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import ch.ethz.idsc.amodeus.routing.NetworkTimeDistInterface;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum LatestArrival {
    ;

    public static Scalar of(AVRequest avRequest, Scalar maxDropoffDelay, //
            NetworkTimeDistInterface travelTimeCashed, double timeNow) {
        // TODO possibly simplified grid-cell travel time used in publication, check
        return Quantity.of(avRequest.getSubmissionTime(), SI.SECOND) //
                .add(travelTimeCashed.travelTime(avRequest.getFromLink(), avRequest.getToLink(), timeNow)) //
                .add(maxDropoffDelay);
    }
}
