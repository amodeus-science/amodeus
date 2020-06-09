/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.traveldata;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.Objects;
import java.util.zip.DataFormatException;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.io.Import;

public enum TravelDataIO {
    ;

    /** Saves travelData as a bitmap file
     * 
     * @param file
     * @param travelData
     * @throws IOException */
    public static void writeStatic(File file, StaticTravelData travelData) throws IOException {
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
    /* package */ static StaticTravelData readStatic(File file, VirtualNetwork<Link> virtualNetwork) //
            throws ClassNotFoundException, DataFormatException, IOException {
        try {
            GlobalAssert.that(Objects.nonNull(virtualNetwork));
            StaticTravelData travelData = Import.object(file);
            travelData.checkConsistency();
            travelData.checkIdenticalVirtualNetworkID(virtualNetwork.getvNetworkID());
            return travelData;
        } catch (InvalidClassException e) {
            System.err.println("You're seeing an InvalidClassException. " + "This is likely the case because the internal class of TravelData has changed. "
                    + "Please re-generated your TravelData input / re-run the preparer.");
            throw e;
        }
    }
}
