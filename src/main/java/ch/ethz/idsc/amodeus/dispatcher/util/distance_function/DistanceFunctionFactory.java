package ch.ethz.idsc.amodeus.dispatcher.util.distance_function;

import org.matsim.api.core.v01.network.Network;

public interface DistanceFunctionFactory {
    DistanceFunction createDistanceFunction(Network network);
}
