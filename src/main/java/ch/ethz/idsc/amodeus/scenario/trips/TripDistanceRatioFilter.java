/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.function.Predicate;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;

/** This filter is used to remove {@link TaxiTrip}s in which the recorded distance is
 * too great compared to the minimum distance necessary in the network to go from
 * the trip origin to destination. */
/* package */ class TripDistanceRatioFilter implements Predicate<TaxiTrip> {
    private final Scalar networkDistanceRatio;
    private final Double staticTimeNow = 0.0;
    private final Network network;

    public TripDistanceRatioFilter(Scalar networkDistanceRatio, Network network) {
        this.networkDistanceRatio = networkDistanceRatio;
        this.network = network;
    }

    @Override
    public boolean test(TaxiTrip t) {
        if (network.getNodes().isEmpty())
            return true;
        else {
            Scalar minDistance = StaticHelper.getMinNetworkTripDistance(t, network, staticTimeNow);
            Scalar ratio = t.distance.divide(minDistance);
            return Scalars.lessEquals(ratio, networkDistanceRatio);
        }
    }
}
