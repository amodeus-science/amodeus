/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.traveldata;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;

public enum TravelDataGet {
    ;

    public static TravelData readDefault(VirtualNetwork<Link> virtualNetwork) throws IOException {
        GlobalAssert.that(Objects.nonNull(virtualNetwork));
        ScenarioOptions scenarioOptions = ScenarioOptions.load(MultiFileTools.getWorkingDirectory());
        final File travelDataFile = new File(scenarioOptions.getVirtualNetworkName(), //
                scenarioOptions.getTravelDataName());
        System.out.println("loading travelData from " + travelDataFile.getAbsoluteFile());
        try {
            return TravelDataIO.read(travelDataFile, virtualNetwork);
        } catch (Exception e) {
            System.err.println("cannot load default " + travelDataFile);
            e.printStackTrace();
        }
        return null;
    }

}
