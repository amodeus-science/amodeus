package ch.ethz.idsc.amodeus.dispatcher.util.distance_function;

import org.matsim.api.core.v01.network.Network;

public class EuclideanDistanceFunctionFactory implements DistanceFunctionFactory {
    @Override
    public DistanceFunction createDistanceFunction(Network network) {
        return new EuclideanDistanceFunction();
    }
}
