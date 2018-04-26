package ch.ethz.idsc.amodeus.dispatcher.distance;

import org.matsim.api.core.v01.network.Network;

/** @author sebhoerl */
public class AStarLandmarksDistanceFunctionFactory implements DistanceFunctionFactory {
    private final double overdoFactor;
    private final int numberOfLandmarks;

    public AStarLandmarksDistanceFunctionFactory(int numberOfLandmarks, double overdoFactor) {
        this.overdoFactor = overdoFactor;
        this.numberOfLandmarks = numberOfLandmarks;
    }

    @Override
    public DistanceFunction createDistanceFunction(Network network) {
        return new NetworkDistanceFunction(network, new AStarLandmarksFactory(numberOfLandmarks, overdoFactor));
    }
}
