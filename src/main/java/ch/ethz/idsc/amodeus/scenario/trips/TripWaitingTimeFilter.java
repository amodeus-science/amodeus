package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;

/* package */ class TripWaitingTimeFilter implements DataFilter<Trip> {
    private final double maxWaitTime;

    public TripWaitingTimeFilter(double maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public Stream<Trip> filter(Stream<Trip> stream, ScenarioOptions simOptions, Network network) {
        return stream.filter(trip -> trip.WaitTime <= maxWaitTime);
    }

}
