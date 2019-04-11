/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.NetworkWriter;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.io.GZHandler;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum NetworkPreparer {
    ;

    public static Network run(Network network, ScenarioOptions scenOptions) {
        GlobalAssert.that(Objects.nonNull(network));
        System.out.println("++++++++++++++++++++++++ NETWORK PREPARER +++++++++++++++++++++++++++++++");
        System.out.println("Network (Link) size original: " + network.getLinks().values().size());
        Network modifiedNetwork = network;

        /** put any network modifications, e.g., cutting, here */

        modifiedNetwork.setName(network.getName() + "_prepared");
        modifiedNetwork.setCapacityPeriod(network.getCapacityPeriod());
        modifiedNetwork.setEffectiveCellSize(network.getEffectiveCellSize());
        modifiedNetwork.setEffectiveLaneWidth(network.getEffectiveLaneWidth());

        // TODO: Remove the part about boundary!!!!!; May keep the part on allowedMode
        // in the same way
        // new added: remove the links that don't allow car and links that is outside
        // the boundary of interest
        Set<Id<Link>> linkToRemove = new HashSet<>();
        double x_min = 882925.0888530395;
        double x_max = 899158.1436867907;
        double y_min = 131556.38167303847;
        double y_max = 151575.34454988193;
        for (Id<Link> linkId : network.getLinks().keySet()) {
            if (!network.getLinks().get(linkId).getAllowedModes().contains("car")) {
                // add this link to the too remove set
                linkToRemove.add(linkId);
            }
            double xCoord = network.getLinks().get(linkId).getCoord().getX();
            double yCoord = network.getLinks().get(linkId).getCoord().getY();
            if (xCoord > x_max || xCoord < x_min || yCoord > y_max || yCoord < y_min) {
                linkToRemove.add(linkId);
            }
        }
        for (Id<Link> id : linkToRemove) {
            // remove this link from the network
            modifiedNetwork.removeLink(id);
        }
        System.err.println("links that does not allow car are removed!");
        System.err.println("links that is out of the boundary of interest are removed!");

        // new added: clean network for unconnected links
        NetworkCleaner networkCleaner = new NetworkCleaner();
        networkCleaner.run(modifiedNetwork);
        System.err.println("network cleaned!");

        final File fileExportGz = new File(scenOptions.getPreparedNetworkName() + ".xml.gz");
        final File fileExport = new File(scenOptions.getPreparedNetworkName() + ".xml");
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
