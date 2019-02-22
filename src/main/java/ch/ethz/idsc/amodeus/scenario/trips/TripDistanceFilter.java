package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class TripDistanceFilter implements DataFilter<Trip> {
    private final double maxTripDistance;
    private final double minTripDistance;

    public TripDistanceFilter(double minTripDistance, double maxTripDistance) {
        GlobalAssert.that(minTripDistance < maxTripDistance);
        this.minTripDistance = minTripDistance;
        this.maxTripDistance = maxTripDistance;
    }

    public Stream<Trip> filter(Stream<Trip> stream, ScenarioOptions simOptions, Network network) {
        return stream.filter(trip -> trip.Distance >= minTripDistance && trip.Distance <= maxTripDistance);
    }

}
