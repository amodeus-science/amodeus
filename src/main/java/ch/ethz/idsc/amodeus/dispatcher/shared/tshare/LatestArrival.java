package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import ch.ethz.idsc.amodeus.dispatcher.shared.fifs.TravelTimeCalculatorCached;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.matsim.av.passenger.AVRequest;

public class LatestArrival {

    public static Scalar of(AVRequest avr, Scalar drpoffDelayMax, TravelTimeCalculatorCached travelTimeCashed) {
        // TODO possibly simplified grid-cell travel time used in publication, check
        return Quantity.of(avr.getSubmissionTime(), "s")//
                .add(travelTimeCashed.timeFromTo(avr.getFromLink(), avr.getToLink()))//
                .add(drpoffDelayMax);
    }

}
