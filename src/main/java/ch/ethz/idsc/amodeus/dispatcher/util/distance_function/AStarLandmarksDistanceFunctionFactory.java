package ch.ethz.idsc.amodeus.dispatcher.util.distance_function;

import org.matsim.api.core.v01.network.Network;

public class AStarLandmarksDistanceFunctionFactory implements DistanceFunctionFactory {
    final private double overdoFactor;
    final private int numberOfLandmarks;

    public AStarLandmarksDistanceFunctionFactory(int numberOfLandmarks, double overdoFactor) {
        this.overdoFactor = overdoFactor;
        this.numberOfLandmarks = numberOfLandmarks;
    }

    @Override
    public DistanceFunction createDistanceFunction(Network network) {
        return new NetworkDistanceFunction(network, new AStarLandmarksFactory(numberOfLandmarks, overdoFactor));
    }
}
