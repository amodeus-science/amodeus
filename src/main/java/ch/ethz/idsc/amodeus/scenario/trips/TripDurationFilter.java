/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;

/** Filter used to remove {@link TaxiTrip}s which a duration longer than
 * maxTripDuration. */
public class TripDurationFilter implements DataFilter<TaxiTrip> {
    private final Scalar maxTripDuration;

    public TripDurationFilter(Scalar maxTripDuration) {
        this.maxTripDuration = maxTripDuration;
    }

    @Override
    public Stream<TaxiTrip> filter(Stream<TaxiTrip> stream, ScenarioOptions simOptions, Network network) {
        return stream.filter(trip -> Scalars.lessEquals(trip.duration, maxTripDuration));
    }

}
