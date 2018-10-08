/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;

public enum VirtualNetworkCreators implements VirtualNetworkCreator {
    NONE {
        @Override
        public VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenarioOptions, int numRt, int endTime) {
            return TrivialMatsimVirtualNetwork.createVirtualNetwork(network);
        }
    },
    SHAPEFILENETWORK {
        @Override
        public VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenarioOptions, int numRt, int endTime) {
            GlobalAssert.that(scenarioOptions != null);
            return MatsimShapeFileVirtualNetworkCreator.createVirtualNetwork(network, scenarioOptions);
        }
    },
    KMEANS {
        @Override
        public VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenarioOptions, int numRt, int endTime) {
            GlobalAssert.that(scenarioOptions != null);
            return MatsimKMeansVirtualNetworkCreator.createVirtualNetwork( //
                    population, network, scenarioOptions.getNumVirtualNodes(), scenarioOptions.isCompleteGraph());
        }
    };
}
