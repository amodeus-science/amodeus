/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.io.NetworkWriter;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.io.GZHandler;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum NetworkPreparer {
    ;

    public static Network run(Network network, ScenarioOptions scenOptions, File workingDirectory) {
        GlobalAssert.that(Objects.nonNull(network));
        System.out.println("++++++++++++++++++++++++ NETWORK PREPARER +++++++++++++++++++++++++++++++");
        System.out.println("Network (Link) size original: " + network.getLinks().values().size());
        Network modifiedNetwork = network;

        /** put any network modifications, e.g., cutting, here */

        modifiedNetwork.setName(network.getName() + "_prepared");
        modifiedNetwork.setCapacityPeriod(network.getCapacityPeriod());
        modifiedNetwork.setEffectiveCellSize(network.getEffectiveCellSize());
        modifiedNetwork.setEffectiveLaneWidth(network.getEffectiveLaneWidth());

        final File fileExportGz = new File(workingDirectory, scenOptions.getPreparedNetworkName() + ".xml.gz");
        final File fileExport = new File(workingDirectory, scenOptions.getPreparedNetworkName() + ".xml");
        {
            // write the modified population to file
            NetworkWriter nw = new NetworkWriter(modifiedNetwork);
            nw.write(fileExportGz.toString());
        }
        // extract the created .gz file
        try {
            GZHandler.extract(fileExportGz, fileExport);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("saved converted network to: " + scenOptions.getPreparedNetworkName() + ".xml");
        return modifiedNetwork;
    }

}
