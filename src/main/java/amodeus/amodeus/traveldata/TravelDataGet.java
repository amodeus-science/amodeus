/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.traveldata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.DataFormatException;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;

public enum TravelDataGet {
    ;

    public static StaticTravelData readFile(VirtualNetwork<Link> virtualNetwork, File path) throws ClassNotFoundException, DataFormatException, IOException {
        if (Objects.isNull(virtualNetwork))
            throw new IllegalStateException("Cannot read travel data if virtual network is null");

        if (!path.exists())
            throw new FileNotFoundException(path.toString());

        return TravelDataIO.readStatic(path, virtualNetwork);
    }

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
            return null;
        }
    }
}
