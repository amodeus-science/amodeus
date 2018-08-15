/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;

public enum VirtualNetworkCreators implements VirtualNetworkCreator {
    SHAPEFILENETWORK {
        @Override
        public VirtualNetwork<Link> create(Network network, Population population) {
            GlobalAssert.that(scenarioOptions != null);
            return MatsimShapeFileVirtualNetworkCreator.creatVirtualNetwork(network, scenarioOptions);
        }
    },
    KMEANS {
        @Override
        public VirtualNetwork<Link> create(Network network, Population population) {
            GlobalAssert.that(scenarioOptions != null);
            return MatsimKMeansVirtualNetworkCreator.createVirtualNetwork( //
                    population, network, scenarioOptions.getNumVirtualNodes(), scenarioOptions.isCompleteGraph());
        }
    },
    KMEANSCASCADE {
        @Override
        public VirtualNetwork<Link> create(Network network, Population population) {
            GlobalAssert.that(scenarioOptions != null);
            return MatsimKMeansCascadeVirtualNetworkCreator.createVirtualNetwork( //
                    population, network, scenarioOptions.getNumVirtualNodes(), scenarioOptions.isCompleteGraph());
        }
    };
    protected ScenarioOptions scenarioOptions = null;

    public void setScenarioOptions(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
    }

}
