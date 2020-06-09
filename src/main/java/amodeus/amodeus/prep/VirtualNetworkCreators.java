/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.prep;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import amodeus.amodeus.virtualnetwork.core.VirtualNetworkIO;

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
            File absFileName = new File(scenarioOptions.getWorkingDirectory(), scenarioOptions.getString("vnFile"));
            Map<String, Link> map = network.getLinks().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
            try {
                return VirtualNetworkIO.fromByte(map, absFileName);
            } catch (Exception exception) {
                exception.printStackTrace();
                return TrivialMatsimVirtualNetwork.createVirtualNetwork(network);
            }
        }
    },
    SHAPEFILENETWORK {
        @Override
        public VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenarioOptions, int numRt, int endTime) {
            return MatsimShapeFileVirtualNetworkCreator.createVirtualNetwork(network, Objects.requireNonNull(scenarioOptions));
        }
    },
    KMEANS {
        @Override
        public VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenarioOptions, int numRt, int endTime) {
            return MatsimKMeansVirtualNetworkCreator.createVirtualNetwork(population, network, //
                    Objects.requireNonNull(scenarioOptions).getNumVirtualNodes(), scenarioOptions.isCompleteGraph());
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
