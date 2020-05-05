/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.FastAStarEuclideanFactory;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.FastDijkstraFactory;

import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.routing.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.routing.NetworkMinTimeDistanceFunction;

/**
 * Enum of various methods to compute distances between two points on a network - used in the dispatching logic
 *
 * EUCLIDEAN - euclidean distance function
 * DIJKSTRA - computes the shortest paths in terms of free flow travel times according to Dijkstra's algorithm
 * ASTAR - computes the shortest paths in terms of free flow travel times using an AStar algorithm with euclidean
 *         lower bounds
 * ASTARLANDMARKS - computes the shortest paths in terms of free flow travel times using an AStar algorithm with lower
 *  bounds computed using known (euclidean) distances to Landmarks and the triangle inequality
 *
 *  //todo @clruch In AStar and ASTARLANDMARKS is this correct? Euclidean distances are not valid lower bounds on travel
 *  //todo @clruch I am missing a few pieces of the code, that's why I am asking - not sure I fully understand.
 */
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
            return new NetworkMinTimeDistanceFunction(network, new FastAStarLandmarksFactory(Runtime.getRuntime().availableProcessors()));
        }
    };

    public abstract DistanceFunction getDistanceFunction(Network network);
}