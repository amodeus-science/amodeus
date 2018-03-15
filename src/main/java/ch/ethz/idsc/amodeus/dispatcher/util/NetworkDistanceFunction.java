/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.router.util.LeastCostPathCalculator;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

public class NetworkDistanceFunction implements DistanceFunction {
    private final LeastCostPathCalculator dijkstra;

    public NetworkDistanceFunction(Network network) {
        dijkstra = SimpleDijkstra.prepDijkstra(network);
    }

    @Override
    public double getDistance(RoboTaxi robotaxi, AVRequest avrequest) {

        Node from = robotaxi.getDivertableLocation().getFromNode();
        Node to = avrequest.getFromLink().getFromNode();

        return distNetwork(from, to);

    }

    @Override
    public double getDistance(RoboTaxi robotaxi, Link link) {

        Node from = robotaxi.getDivertableLocation().getFromNode();
        Node to = link.getFromNode();

        return distNetwork(from, to);

    }

    @Override
    public double getDistance(Link link1, Link link2) {

        Node from = link1.getFromNode();
        Node to = link2.getFromNode();

        return distNetwork(from, to);

    }

    /** @param from non null
     * @param to non null
     * @return */
    private double distNetwork(Node from, Node to) {
        double dist = 0.0;
        LeastCostPathCalculator.Path path = SimpleDijkstra.executeDijkstra(dijkstra, from, to);
        for (Link link : path.links) {
            dist += link.getLength();
        }
        return dist;
    }

}
