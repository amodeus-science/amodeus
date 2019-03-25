/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.traveldata;

import java.io.File;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;

public enum TravelDataGet {
    ;

    public static StaticTravelData readStatic(VirtualNetwork<Link> virtualNetwork, ScenarioOptions scenarioOptions) {
        GlobalAssert.that(Objects.nonNull(virtualNetwork));
        final File travelDataFile = new File(scenarioOptions.getVirtualNetworkDirectoryName(), //
                scenarioOptions.getTravelDataName());
        System.out.println("loading travelData from " + travelDataFile.getAbsoluteFile());
        try {
            return TravelDataIO.readStatic(travelDataFile, virtualNetwork);
        } catch (Exception e) {
            System.err.println("cannot load default " + travelDataFile);
            e.printStackTrace();
        }
        return null;
    }

}
