/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkIO;

public enum VirtualNetworkCreators implements VirtualNetworkCreator {
    NONE {
        @Override
        public VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenarioOptions, int numRt, int endTime) {
            return TrivialMatsimVirtualNetwork.createVirtualNetwork(network);
        }
    },
    FROMFILE {
        @Override
        public VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenarioOptions, int numRt, int endTime) {
            String absFileName = scenarioOptions.getString("vnFile");
            Map<String, Link> map = new HashMap<>();
            network.getLinks().entrySet().forEach(e -> map.put(e.getKey().toString(), e.getValue()));
            try {
                return VirtualNetworkIO.fromByte(map, new File(absFileName));
            } catch (Exception exception) {
                exception.printStackTrace();
                return TrivialMatsimVirtualNetwork.createVirtualNetwork(network);
            }
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
    },
    RINGCENTROID {
        @Override
        public VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenarioOptions, int numRoboTaxis, int endTime) {
            return MatsimRingCentroidVirtualNetworkCreator.createVirtualNetwork(population, network, 20, scenarioOptions.isCompleteGraph());
        }
    },
    RECTANGULAR {
        @Override
        public VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenarioOptions, int numRoboTaxis, int endTime) {
            int divLat = Integer.parseInt(scenarioOptions.getString("LATITUDE_NODES"));
            int divLng = Integer.parseInt(scenarioOptions.getString("LONGITUDE_NODES"));
            GlobalAssert.that(divLat > 0 && divLng > 0);
            return MatsimRectangleVirtualNetworkCreator.createVirtualNetwork(population, network, scenarioOptions.isCompleteGraph(), divLat, divLng);
        }
    };
}
