/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.FastAStarEuclideanFactory;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.FastDijkstraFactory;

import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.routing.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.routing.NetworkMinTimeDistanceFunction;

public enum DistanceHeuristics {
    EUCLIDEAN {
        @Override
        public DistanceFunction getDistanceFunction(Network network) {
            return EuclideanDistanceFunction.INSTANCE;
        }
    },
    DIJKSTRA {
        @Override
        public DistanceFunction getDistanceFunction(Network network) {
            return new NetworkMinTimeDistanceFunction(network, new FastDijkstraFactory());
        }
    },
    ASTAR {
        @Override
        public DistanceFunction getDistanceFunction(Network network) {
            return new NetworkMinTimeDistanceFunction(network, new FastAStarEuclideanFactory());
        }
    },
    ASTARLANDMARKS {
        @Override
        public DistanceFunction getDistanceFunction(Network network) {
            return new NetworkMinTimeDistanceFunction(network, new FastAStarLandmarksFactory());
        }
    };

    public abstract DistanceFunction getDistanceFunction(Network network);
}