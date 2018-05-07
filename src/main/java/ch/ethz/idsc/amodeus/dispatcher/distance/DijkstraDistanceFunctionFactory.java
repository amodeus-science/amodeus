package ch.ethz.idsc.amodeus.dispatcher.distance;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.FastDijkstraFactory;

/** @author sebhoerl */
public class DijkstraDistanceFunctionFactory implements DistanceFunctionFactory {
    @Override
    public DistanceFunction createDistanceFunction(Network network) {
        return new NetworkDistanceFunction(network, new FastDijkstraFactory());
    }
}
