package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;

public class TripDurationFilter implements DataFilter<Trip> {
    private final double maxTripDuration;

    public TripDurationFilter(long maxTripDuration) {
        this.maxTripDuration = maxTripDuration;
    }

    public Stream<Trip> filter(Stream<Trip> stream, ScenarioOptions simOptions, Network network) {
        return stream.filter(trip -> trip.Duration <= maxTripDuration);
    }

}
