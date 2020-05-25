/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

import ch.ethz.idsc.amodeus.analysis.Analysis;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;

public enum VirtualNetworkGet {
    ;

    /** @param network
     * @throws IOException, FileNotFoundException
     * @throws DataFormatException
     * @throws ClassNotFoundException */
    public static VirtualNetwork<Link> readFile(Network network, File path) throws IOException, ClassNotFoundException, DataFormatException {
        if (!path.exists())
            throw new FileNotFoundException(path.toString());
        return VirtualNetworkIO.fromByte(mapByIdString(network), path);
    }

    /** @param network
     * @return null if file does not exist
     * @throws IOException */
    public static VirtualNetwork<Link> readDefault(Network network, ScenarioOptions scenarioOptions) throws IOException {
        final File virtualnetworkFile = new File(scenarioOptions.getVirtualNetworkDirectoryName(), scenarioOptions.getVirtualNetworkName());
        System.out.println("reading network from" + virtualnetworkFile.getAbsoluteFile());
        try {
            return VirtualNetworkIO.fromByte(mapByIdString(network), virtualnetworkFile);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("cannot load default " + virtualnetworkFile);
        }
        return null;
    }

    /** @param network
     * @return null if file does not exist
     * @throws IOException */
    public static VirtualNetwork<Link> readFromOutputDirectory(Network network, File outputDirectory, ScenarioOptions scenarioOptions) throws IOException {
        final File virtualnetworkFolder = new File(outputDirectory, Analysis.DATAFOLDERNAME);
        final File virtualnetworkFile = new File(virtualnetworkFolder, scenarioOptions.getVirtualNetworkName());
        System.out.println("reading virtual network from" + virtualnetworkFile.getAbsoluteFile());
        try {
            return VirtualNetworkIO.fromByte(mapByIdString(network), virtualnetworkFile);
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
    // return VirtualNetworkIO.fromByte(mapByIdString(network), virtualnetworkFile);
    // } catch (Exception e) {
    // System.out.println("cannot load default " + virtualnetworkFile);
    // }
    // return null;
    // }

    private static Map<String, Link> mapByIdString(Network network) {
        return network.getLinks().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }
}
