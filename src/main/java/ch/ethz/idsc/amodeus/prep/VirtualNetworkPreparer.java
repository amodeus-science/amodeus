/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.File;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.traveldata.StaticTravelData;
import ch.ethz.idsc.amodeus.traveldata.StaticTravelDataCreator;
import ch.ethz.idsc.amodeus.traveldata.TravelDataIO;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkIO;

public enum VirtualNetworkPreparer implements VirtualNetworkCreator {
    INSTANCE;
    // ---

    @Override
    public VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenarioOptions, int numVehicles, int endTime) {
        VirtualNetworkCreator virtualNetworkCreators = scenarioOptions.getVirtualNetworkCreator();
        VirtualNetwork<Link> virtualNetwork = virtualNetworkCreators.create(network, population, scenarioOptions, numVehicles, endTime);
        GlobalAssert.that(Objects.nonNull(virtualNetwork));

        final File vnDir = new File(scenarioOptions.getVirtualNetworkDirectoryName());
        System.out.println("vnDir = " + vnDir.getAbsolutePath());
        vnDir.mkdir(); // create folder if necessary
        GlobalAssert.that(vnDir.isDirectory());

        try {
            VirtualNetworkIO.toByte(new File(vnDir, scenarioOptions.getVirtualNetworkName()), virtualNetwork);
            System.out.println("saved virtual network byte format to : " + new File(vnDir, scenarioOptions.getVirtualNetworkDirectoryName()));

            virtualNetwork.printVirtualNetworkInfo();
            System.out.println("successfully converted simulation data files from in " + scenarioOptions.getWorkingDirectory());

            /** reading the whole travel data */
            StaticTravelData travelData = StaticTravelDataCreator.create(scenarioOptions.getWorkingDirectory(), virtualNetwork, network, population,
                    scenarioOptions.getdtTravelData(), numVehicles, endTime);

            File travelDataFile = new File(scenarioOptions.getVirtualNetworkDirectoryName(), scenarioOptions.getTravelDataName());
            TravelDataIO.writeStatic(travelDataFile, travelData);

        } catch (Exception exception) {
            exception.printStackTrace();

            throw new RuntimeException();
        }

        return virtualNetwork;
    }
}
