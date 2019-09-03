/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.function.Predicate;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;

/** {@link TaxiTrip} filter used to remove taxi {@link TaxiTrip}s with a maximum wait time
 * which is too long. */
/* package */ class TripWaitingTimeFilter implements Predicate<TaxiTrip> {
    private final Scalar maxWaitTime;

    public TripWaitingTimeFilter(Scalar maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }


    @Override
    public boolean test(TaxiTrip t) {
        return Scalars.lessEquals(t.waitTime, maxWaitTime);
    }

}
