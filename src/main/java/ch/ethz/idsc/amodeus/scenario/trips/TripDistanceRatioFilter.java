/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;

/** This filter is used to remove {@link TaxiTrip}s in which the recorded distance is
 * too great compared to the minimum distance necessary in the network to go from
 * the trip origin to destination. */
/* package */ class TripDistanceRatioFilter implements DataFilter<TaxiTrip> {
    private final Scalar networkDistanceRatio;
    private final Double staticTimeNow = 0.0;

    public TripDistanceRatioFilter(Scalar networkDistanceRatio) {
        this.networkDistanceRatio = networkDistanceRatio;
    }

    @Override
    public Stream<TaxiTrip> filter(Stream<TaxiTrip> stream, ScenarioOptions simOptions, Network network) {
        if (network.getNodes().isEmpty()) {
            System.err.println("WARN network seems to be empty.");
            return stream;
        }
        return stream.filter(trip -> {
            Scalar minDistance = StaticHelper.getMinNetworkTripDistance(trip, network, staticTimeNow);
            Scalar ratio = trip.distance.divide(minDistance);
            return Scalars.lessEquals(ratio, networkDistanceRatio);
        });
    }

}
