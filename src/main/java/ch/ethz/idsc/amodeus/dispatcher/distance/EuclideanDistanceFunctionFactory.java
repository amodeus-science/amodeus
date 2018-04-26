package ch.ethz.idsc.amodeus.dispatcher.distance;

import org.matsim.api.core.v01.network.Network;

/** @author sebhoerl */
public class EuclideanDistanceFunctionFactory implements DistanceFunctionFactory {
    @Override
    public DistanceFunction createDistanceFunction(Network network) {
        return new EuclideanDistanceFunction();
    }
}
