package ch.ethz.idsc.amodeus.dispatcher.distance;

import org.matsim.api.core.v01.network.Network;

/** @author sebhoerl */
public class AStarEuclideanDistanceFunctionFactory implements DistanceFunctionFactory {
    private final double overdoFactor;

    public AStarEuclideanDistanceFunctionFactory(double overdoFactor) {
        this.overdoFactor = overdoFactor;
    }

    @Override
    public DistanceFunction createDistanceFunction(Network network) {
        return new NetworkDistanceFunction(network, new AStarEuclideanFactory(overdoFactor));
    }
}
