/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;

/** This {@link TaxiTrip} filter is used to remove {@link TaxiTrip}s with a recorded distance
 * smaller than a minimumDistance or larger than a maximum distance. */
public class TripDistanceFilter implements DataFilter<TaxiTrip> {
    private final Scalar maxTripDistance;
    private final Scalar minTripDistance;

    public TripDistanceFilter(Scalar minTripDistance, Scalar maxTripDistance) {
        GlobalAssert.that(Scalars.lessThan(minTripDistance, maxTripDistance));
        this.minTripDistance = minTripDistance;
        this.maxTripDistance = maxTripDistance;
    }

    public Stream<TaxiTrip> filter(Stream<TaxiTrip> stream, ScenarioOptions simOptions, Network network) {
        return stream//
                .filter(trip -> Scalars.lessEquals(minTripDistance, trip.distance))//
                .filter(trip -> Scalars.lessEquals(trip.distance, maxTripDistance));
    }
}
