/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;

import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.io.Import;

/** Helper class to save/load {@link LinkSpeedDataContainer}s to the file system.
 * {@link LinkSpeedDataContainer} are used to save a certain traffic status, e.g., as found
 * in a dataset of taxi traces. */
public enum LinkSpeedUtils {
    ;

    /** @return {@link LinkSpeedDataContainer} in {@link File} @param inputFile
     * 
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws DataFormatException */
    public static LinkSpeedDataContainer loadLinkSpeedData(File inputFile) {
        try {
            return Import.object(inputFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /** Writes the {@link LinkSpeedDataContainer} @param lsData to the location @param file
     * 
     * @throws IOException if the operation fails */
    public static void writeLinkSpeedData(File file, LinkSpeedDataContainer lsData) throws IOException {
        Export.object(file, lsData);
        System.out.println("LinkSpeedData exported to: " + file.getAbsolutePath());
    }
}
