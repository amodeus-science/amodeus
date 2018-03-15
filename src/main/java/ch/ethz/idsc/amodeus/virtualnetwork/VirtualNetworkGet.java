/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;

public enum VirtualNetworkGet {
    ;

    /** @param network
     * @return null if file does not exist
     * @throws IOException */
    public static VirtualNetwork<Link> readDefault(Network network) throws IOException {

        ScenarioOptions scenarioOptions = ScenarioOptions.load(MultiFileTools.getWorkingDirectory());
        final File virtualnetworkFile = new File(scenarioOptions.getVirtualNetworkName(), scenarioOptions.getVirtualNetworkName());
        System.out.println("reading network from" + virtualnetworkFile.getAbsoluteFile());
        try {

            Map<String, Link> map = new HashMap<>();
            network.getLinks().entrySet().forEach(e -> map.put(e.getKey().toString(), e.getValue()));

            return VirtualNetworkIO.fromByte(map, virtualnetworkFile);
        } catch (Exception e) {
            System.out.println("cannot load default " + virtualnetworkFile);

        }
        return null;
    }

}
