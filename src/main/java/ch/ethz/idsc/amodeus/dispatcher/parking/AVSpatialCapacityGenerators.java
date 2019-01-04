/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum AVSpatialCapacityGenerators implements AVSpatialCapacityGenerator {
    NONE {
        @Override
        public AVSpatialCapacityAmodeus generate(Network network) {
            return new AVSpatialCapacityInfinity(network);
        }
    },
    NETWORKBASED {
        @Override
        public AVSpatialCapacityAmodeus generate(Network network) {
            GlobalAssert.that(scenarioOptions != null);
            return new AVSpatialCapacityFromNetworkAndIdentifier(network, scenarioOptions.getParkingSpaceTagInNetwork());
        }
    };

    protected ScenarioOptions scenarioOptions = null;

    public AVSpatialCapacityGenerators setScenarioOptions(ScenarioOptions scenarioOptions) {
        this.scenarioOptions = scenarioOptions;
        return this;
    }

}
