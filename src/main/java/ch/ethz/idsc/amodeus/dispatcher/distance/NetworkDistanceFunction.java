/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.distance;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.EasyPathCalculator;
import ch.ethz.idsc.owl.data.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

public class NetworkDistanceFunction implements DistanceFunction {
    private final LeastCostPathCalculator pathCalc;

    public NetworkDistanceFunction(Network network, LeastCostPathCalculatorFactory calcFactory) {
        pathCalc = EasyPathCalculator.prepPathCalculator(network, calcFactory);
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

    // Added Nicolo 29-10-17
    @Override
    public double getDistance(Link link1, Link link2) {

        Node from = link1.getFromNode();
        Node to = link2.getFromNode();

        return distNetwork(from, to);
    }

    private double distNetwork(Node from, Node to) {
        GlobalAssert.that(from != null);
        GlobalAssert.that(to != null);
        double dist = 0.0;
        LeastCostPathCalculator.Path path = EasyPathCalculator.execPathCalculator(pathCalc, from, to);
        for (Link link : path.links) {
            dist += link.getLength();
        }
        return dist;
    }
}
