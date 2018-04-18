package ch.ethz.idsc.amodeus.dispatcher.util.distance_function;

import org.matsim.api.core.v01.network.Network;

public class AStarEuclideanDistanceFunctionFactory implements DistanceFunctionFactory {
    final private double overdoFactor;

    public AStarEuclideanDistanceFunctionFactory(double overdoFactor) {
        this.overdoFactor = overdoFactor;
    }

    @Override
    public DistanceFunction createDistanceFunction(Network network) {
        return new NetworkDistanceFunction(network, new AStarEuclideanFactory(overdoFactor));
    }
}
