/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;

// TODO could this be replaced by CashedNetworkTimeDistance?
public class NetworkMinTimeDistanceFunction extends NetworkDistanceFunction {

    public NetworkMinTimeDistanceFunction(Network network, LeastCostPathCalculatorFactory calcFactory) {
        super(EasyMinTimePathCalculator.prepPathCalculator(network, calcFactory));
    }
}
