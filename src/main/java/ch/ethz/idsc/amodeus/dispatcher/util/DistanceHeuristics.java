/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.FastAStarEuclideanFactory;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.FastDijkstraFactory;

public enum DistanceHeuristics {
    EUCLIDEAN {
        @Override
        public DistanceFunction getDistanceFunction(Network network) {
            return new EuclideanDistanceFunction();
        }
    },
    EUCLIDEANNONCYCLIC {
        @Override
        public DistanceFunction getDistanceFunction(Network network) {
            return new NonCyclicDistanceFunction(new NetworkDistanceFunction(network, new FastAStarLandmarksFactory()));
        }
    },
    DIJKSTRA {
        @Override
        public DistanceFunction getDistanceFunction(Network network) {
            return new NetworkDistanceFunction(network, new FastDijkstraFactory());
        }
    },
    ASTAR {
        @Override
        public DistanceFunction getDistanceFunction(Network network) {
            return new NetworkDistanceFunction(network, new FastAStarEuclideanFactory());
        }
    },
    ASTARLANDMARKS {
        @Override
        public DistanceFunction getDistanceFunction(Network network) {
            return new NetworkDistanceFunction(network, new FastAStarLandmarksFactory());
        }
    };

    public abstract DistanceFunction getDistanceFunction(Network network);
}