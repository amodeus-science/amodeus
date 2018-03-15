/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;

public enum VirtualNetworkCreators {
    SHAPEFILENETWORK {
        @Override
        public VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenarioOptions) {
            return MatsimShapeFileVirtualNetworkCreator.creatVirtualNetwork(network, scenarioOptions);
        }
    },
    KMEANS {
        @Override
        public VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenOptions) {
            return MatsimKMEANSVirtualNetworkCreator.createVirtualNetwork( //
                    population, network, scenOptions.getNumVirtualNodes(), scenOptions.isCompleteGraph());
        }
    };

    public abstract VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenOptions);

}
