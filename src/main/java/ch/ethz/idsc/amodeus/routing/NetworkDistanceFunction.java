/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import org.matsim.amodeus.dvrp.request.AVRequest;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.router.util.LeastCostPathCalculator;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/* package */ abstract class NetworkDistanceFunction implements DistanceFunction {

    private final LeastCostPathCalculator leastCostPathCalculator;

    public NetworkDistanceFunction(LeastCostPathCalculator pathCalc) {
        this.leastCostPathCalculator = pathCalc;
    }

    @Override
    public final double getDistance(RoboTaxi roboTaxi, AVRequest avrequest) {
        Node from = roboTaxi.getDivertableLocation().getFromNode();
        Node to = avrequest.getFromLink().getFromNode();
        return distNetwork(from, to);
    }

    @Override
    public final double getDistance(RoboTaxi roboTaxi, Link link) {
        Node from = roboTaxi.getDivertableLocation().getFromNode();
        Node to = link.getFromNode();
        return distNetwork(from, to);
    }

    @Override
    public final double getDistance(Link link1, Link link2) {
        Node from = link1.getFromNode();
        Node to = link2.getFromNode();
        return distNetwork(from, to);
    }

    public final double getTravelTime(Link from, Link to) {
        return getTravelTime(from.getFromNode(), to.getFromNode()); // should this be to.getFromNode or to.getToNode?
    }

    private double getTravelTime(Node from, Node to) {
        LeastCostPathCalculator.Path path = execPathCalculator(from, to);
        return path.travelTime;
    }

    private final double distNetwork(Node from, Node to) {
        LeastCostPathCalculator.Path path = execPathCalculator(from, to);
        return PathProperty.length(path);
    }

    private LeastCostPathCalculator.Path execPathCalculator(Node from, Node to) {
        // depending on implementation of traveldisutility and traveltime, starttime,
        // person and vehicle are needed
        return PathProperty.fromTo(from, to, leastCostPathCalculator, 0.0);
    }
}
