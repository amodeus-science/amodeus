/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;

public enum VirtualNetworkGet {
    ;

    /** @param network
     * @return null if file does not exist
     * @throws IOException */
    public static VirtualNetwork<Link> readDefault(Network network, ScenarioOptions scenarioOptions) throws IOException {
        final File virtualnetworkFile = new File(scenarioOptions.getVirtualNetworkDirectoryName(), scenarioOptions.getVirtualNetworkName());
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

    /** @param network
     * @return null if file does not exist
     * @throws IOException */
    public static VirtualNetwork<Link> readFromOutputDirectory(Network network, File outputDirectory, ScenarioOptions scenarioOptions) throws IOException {
        final File virtualnetworkFolder = new File(outputDirectory, "data"); // TODO Joel still hard-coded, search for "data" in project
        final File virtualnetworkFile = new File(virtualnetworkFolder, scenarioOptions.getVirtualNetworkName());
        System.out.println("reading virtual network from" + virtualnetworkFile.getAbsoluteFile());
        try {
            Map<String, Link> map = new HashMap<>();
            network.getLinks().entrySet().forEach(e -> map.put(e.getKey().toString(), e.getValue()));
            return VirtualNetworkIO.fromByte(map, virtualnetworkFile);
        } catch (Exception e) {
            System.out.println("cannot load from output directory, reading default... " + virtualnetworkFile);
            return readDefault(network, scenarioOptions);
        }
    }

    // /** @param network
    // * @return null if file does not exist
    // * @throws IOException */
    // public static VirtualNetwork<Link> readFromWorkingDirectory(Network network, ScenarioOptions scenarioOptions) throws IOException {
    // final File virtualnetworkFolder = new File(scenarioOptions.getWorkingDirectory(), scenarioOptions.getVirtualNetworkDirectoryName());
    // final File virtualnetworkFile = new File(virtualnetworkFolder, scenarioOptions.getVirtualNetworkDirectoryName());
    // System.out.println("reading virtual network from" + virtualnetworkFile.getAbsoluteFile());
    // try {
    // Map<String, Link> map = new HashMap<>();
    // network.getLinks().entrySet().forEach(e -> map.put(e.getKey().toString(), e.getValue()));
    // return VirtualNetworkIO.fromByte(map, virtualnetworkFile);
    // } catch (Exception e) {
    // System.out.println("cannot load default " + virtualnetworkFile);
    // }
    // return null;
    // }

}
