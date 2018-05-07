package ch.ethz.idsc.amodeus.dispatcher.distance;

import org.matsim.api.core.v01.network.Network;

/** @author sebhoerl */
public interface DistanceFunctionFactory {
    DistanceFunction createDistanceFunction(Network network);
}
