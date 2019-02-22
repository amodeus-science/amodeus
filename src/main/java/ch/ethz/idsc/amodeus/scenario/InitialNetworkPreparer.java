package ch.ethz.idsc.amodeus.scenario;

import java.io.File;
import java.io.IOException;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.NetworkWriter;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.scenario.chicago.LinkModes;
import ch.ethz.idsc.amodeus.scenario.chicago.NetworkCutterUtils;
import ch.ethz.idsc.amodeus.util.io.GZHandler;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum InitialNetworkPreparer {
    ;

    public static Network run(File processingDir) {

        // load the pt2matsim network
        Network networkpt2Matsim = NetworkLoader.fromNetworkFile(new File(processingDir, "network.xml"));
        GlobalAssert.that(!networkpt2Matsim.getNodes().isEmpty());

        // remove links on which cars cannot drive
        LinkModes linkModes = new LinkModes("car");
        Network filteredNetwork = NetworkCutterUtils.modeFilter(networkpt2Matsim, linkModes);

        // cleanup the network
        new NetworkCleaner().run(filteredNetwork);

        // save the network
        final File fileExport = new File(processingDir + "/network.xml");
        final File fileExportGz = new File(processingDir + "/network.xml.gz");
        {
            // write the modified population to file
            NetworkWriter nw = new NetworkWriter(filteredNetwork);
            nw.write(fileExportGz.toString());
        }
        try {
            GZHandler.extract(fileExportGz, fileExport);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filteredNetwork;
    }
}