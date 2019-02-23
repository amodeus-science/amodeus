package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;

/* package */ class TripDistanceRatioFilter implements DataFilter<Trip> {
    private final double networkDistanceRatio;

    public TripDistanceRatioFilter(double networkDistanceRatio) {
        this.networkDistanceRatio = networkDistanceRatio;
    }

    public Stream<Trip> filter(Stream<Trip> stream, ScenarioOptions simOptions, Network network) {
        if (network.getNodes().isEmpty()) {
            System.err.println("WARN network seems to be empty.");
            return stream;
        }
        return stream.filter(trip -> {
            double minDistance = StaticHelper.getMinNetworkTripDistance(trip, network);
            double ratio = trip.Distance / minDistance;
            return ratio <= networkDistanceRatio;
        });
    }

}
