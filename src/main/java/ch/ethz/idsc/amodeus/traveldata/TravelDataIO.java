/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.traveldata;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.DataFormatException;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.io.Import;

public enum TravelDataIO {
    ;

    /** Saves travelData as a bitmap file
     * 
     * @param file
     * @param travelData
     * @throws IOException */
    public static void write(File file, TravelData travelData) throws IOException {
        travelData.checkConsistency();
        Export.object(file, travelData);
    }

    /** loads travelData from a bitmap file, if possible use {@link TravelDataGet.readDefault()}
     * 
     * @param file
     * @param virtualNetwork
     * @return
     * @throws ClassNotFoundException
     * @throws DataFormatException
     * @throws IOException */
    /* package */ static TravelData read(File file, VirtualNetwork<Link> virtualNetwork) //
            throws ClassNotFoundException, DataFormatException, IOException {
        GlobalAssert.that(Objects.nonNull(virtualNetwork));
        TravelData travelData = Import.object(file);
        travelData.checkConsistency();
        travelData.checkIdenticalVirtualNetworkID(virtualNetwork.getvNetworkID());
        return travelData;
    }
}
