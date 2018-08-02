/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;

public enum LPCreator {
    NONE {
        @Override
        public LPSolver create(VirtualNetwork<Link> virtualNetwork, Network network, ScenarioOptions scenarioOptions, TravelData travelData) {
            return new LPEmpty(virtualNetwork, travelData.getLambdaAbsolute());
        }
    },
    TIMEINVARIANT {
        @Override
        public LPSolver create(VirtualNetwork<Link> virtualNetwork, Network network, ScenarioOptions scenarioOptions, TravelData travelData) {
            return new LPTimeInvariant(virtualNetwork, travelData.getLambdaRate());
        }
    },
    TIMEVARIANT {
        @Override
        public LPSolver create(VirtualNetwork<Link> virtualNetwork, Network network, ScenarioOptions scenarioOptions, TravelData travelData) {
            return new LPTimeVariant(virtualNetwork, network, scenarioOptions, travelData.getLambdaAbsolute());
        }
    };

    public abstract LPSolver create(VirtualNetwork<Link> virtualNetwork, Network network, ScenarioOptions scenarioOptions, TravelData travelData);
}
