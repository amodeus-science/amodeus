/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.File;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.traveldata.TravelDataCreator;
import ch.ethz.idsc.amodeus.traveldata.TravelDataIO;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetworkIO;

public enum VirtualNetworkPreparer {
    ;
    private static VirtualNetwork<Link> virtualNetwork;

    public static void run(Network network, Population population, ScenarioOptions scenarioOptions) throws Exception {

        VirtualNetworkCreator virtualNetworkCreators = scenarioOptions.getVirtualNetworkCreator();
        virtualNetwork = virtualNetworkCreators.create(network, population);

        final File vnDir = new File(scenarioOptions.getVirtualNetworkName());
        System.out.println("vnDir = " + vnDir.getAbsolutePath());
        vnDir.mkdir(); // create folder if necessary
        GlobalAssert.that(Objects.nonNull(virtualNetwork));
        VirtualNetworkIO.toByte(new File(vnDir, scenarioOptions.getVirtualNetworkName()), virtualNetwork);
        System.out.println("saved virtual network byte format to : " + new File(vnDir, scenarioOptions.getVirtualNetworkName()));

        virtualNetwork.printVirtualNetworkInfo();
        System.out.println("successfully converted simulation data files from in " + MultiFileTools.getWorkingDirectory());

        /** reading the customer requests */
        TravelData travelData = TravelDataCreator.create(virtualNetwork, network, population, scenarioOptions);

        File travelDataFile = new File(scenarioOptions.getVirtualNetworkName(), scenarioOptions.getTravelDataName());
        TravelDataIO.write(travelDataFile, travelData);
    }

    public static VirtualNetwork<Link> getVirtualNetwork() {
        return virtualNetwork;
    }
}
