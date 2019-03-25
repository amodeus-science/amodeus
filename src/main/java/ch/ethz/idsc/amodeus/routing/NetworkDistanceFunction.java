/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.router.util.LeastCostPathCalculator;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

public abstract class NetworkDistanceFunction implements DistanceFunction {

    private final LeastCostPathCalculator pathCalc;

    public NetworkDistanceFunction(LeastCostPathCalculator pathCalc) {
        this.pathCalc = pathCalc;
    }

    @Override
    public final double getDistance(RoboTaxi robotaxi, AVRequest avrequest) {
        Node from = robotaxi.getDivertableLocation().getFromNode();
        Node to = avrequest.getFromLink().getFromNode();
        return distNetwork(from, to);
    }

    @Override
    public final double getDistance(RoboTaxi robotaxi, Link link) {
        Node from = robotaxi.getDivertableLocation().getFromNode();
        Node to = link.getFromNode();
        return distNetwork(from, to);
    }

    @Override
    public final double getDistance(Link link1, Link link2) {
        Node from = link1.getFromNode();
        Node to = link2.getFromNode();
        return distNetwork(from, to);
    }

    public double getTravelTime(Link from, Link to) {
        return getTravelTime(from.getFromNode(), to.getFromNode());
    }

    private double getTravelTime(Node from, Node to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        LeastCostPathCalculator.Path path = EasyMinTimePathCalculator.execPathCalculator(pathCalc, from, to);
        return path.travelTime;
    }

    private final double distNetwork(Node from, Node to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        double dist = 0.0;
        LeastCostPathCalculator.Path path = EasyMinTimePathCalculator.execPathCalculator(pathCalc, from, to);
        for (Link link : path.links) {
            dist += link.getLength();
        }
        return dist;
    }

}
