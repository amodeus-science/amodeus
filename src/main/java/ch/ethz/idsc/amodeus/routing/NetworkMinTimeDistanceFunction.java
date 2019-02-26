/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;

public class NetworkMinTimeDistanceFunction extends NetworkDistanceFunction {

    public NetworkMinTimeDistanceFunction(Network network, LeastCostPathCalculatorFactory calcFactory) {
        super(EasyMinTimePathCalculator.prepPathCalculator(network, calcFactory));
    }
}

// public class NetworkMinTimeDistanceFunction implements DistanceFunction {
// private final LeastCostPathCalculator pathCalc;
//
// public NetworkMinTimeDistanceFunction(Network network, LeastCostPathCalculatorFactory calcFactory) {
// pathCalc = EasyMinTimePathCalculator.prepPathCalculator(network, calcFactory);
// }
//
// @Override
// public double getDistance(RoboTaxi robotaxi, AVRequest avrequest) {
// Node from = robotaxi.getDivertableLocation().getFromNode();
// Node to = avrequest.getFromLink().getFromNode();
// return distNetwork(from, to);
// }
//
// @Override
// public double getDistance(RoboTaxi robotaxi, Link link) {
// Node from = robotaxi.getDivertableLocation().getFromNode();
// Node to = link.getFromNode();
// return distNetwork(from, to);
// }
//
// @Override
// public double getDistance(Link link1, Link link2) {
// Node from = link1.getFromNode();
// Node to = link2.getFromNode();
// return distNetwork(from, to);
// }
//
// private double distNetwork(Node from, Node to) {
// GlobalAssert.that(from != null);
// GlobalAssert.that(to != null);
// double dist = 0.0;
// LeastCostPathCalculator.Path path = EasyMinTimePathCalculator.execPathCalculator(pathCalc, from, to);
// for (Link link : path.links) {
// dist += link.getLength();
// }
// return dist;
// }
// }
