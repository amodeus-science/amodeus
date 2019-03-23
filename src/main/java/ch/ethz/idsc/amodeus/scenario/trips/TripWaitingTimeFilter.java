/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;

/** {@link TaxiTrip} filter used to remove taxi {@link TaxiTrip}s with a maximum wait time
 * which is too long. */
/* package */ class TripWaitingTimeFilter implements DataFilter<TaxiTrip> {
    private final Scalar maxWaitTime;

    public TripWaitingTimeFilter(Scalar maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public Stream<TaxiTrip> filter(Stream<TaxiTrip> stream, ScenarioOptions simOptions, Network network) {
        return stream.filter(trip -> Scalars.lessEquals(trip.waitTime, maxWaitTime));
    }

}
