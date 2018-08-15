/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;

public enum LPCreator {
    NONE {
        @Override
        public LPSolver create(VirtualNetwork<Link> virtualNetwork, Network network, ScenarioOptions scenarioOptions, Tensor lambdaAbsolute) {
            return new LPEmpty(virtualNetwork, lambdaAbsolute);
        }
    },
    TIMEINVARIANT {
        @Override
        public LPSolver create(VirtualNetwork<Link> virtualNetwork, Network network, ScenarioOptions scenarioOptions, Tensor lambdaAbsolute) {
            return new LPTimeInvariant(virtualNetwork, lambdaAbsolute);
        }
    },
    TIMEVARIANT {
        @Override
        public LPSolver create(VirtualNetwork<Link> virtualNetwork, Network network, ScenarioOptions scenarioOptions, Tensor lambdaAbsolute) {
            return new LPTimeVariant(virtualNetwork, network, scenarioOptions, lambdaAbsolute);
        }
    };

    public abstract LPSolver create(VirtualNetwork<Link> virtualNetwork, Network network, ScenarioOptions scenarioOptions, Tensor lambdaAbsolute);
}
