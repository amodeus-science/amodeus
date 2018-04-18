package ch.ethz.idsc.amodeus.dispatcher.util.distance_function;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.FastDijkstraFactory;

public class DijkstraDistanceFunctionFactory implements DistanceFunctionFactory {
    @Override
    public DistanceFunction createDistanceFunction(Network network) {
        return new NetworkDistanceFunction(network, new FastDijkstraFactory());
    }
}
