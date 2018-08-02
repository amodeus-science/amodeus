/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;

import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.io.Import;

enum RebalanceDataIO {
    ;

    /** Saves rebalanceData as a bitmap file
     * 
     * @param file
     * @param rebalanceData
     * @throws IOException */
    public static void write(File file, RebalanceData rebalanceData) throws IOException {
        Export.object(file, rebalanceData);
    }

    /** loads rebalanceData from a bitmap file, if possible use {@link TravelDataGet.readDefault()}
     * 
     * @param file
     * @return RebalanceData
     * @throws ClassNotFoundException
     * @throws DataFormatException
     * @throws IOException */
    /* package */ static RebalanceData read(File file) //
            throws ClassNotFoundException, DataFormatException, IOException {
        RebalanceData rebalanceData = Import.object(file);
        return rebalanceData;
    }
}
