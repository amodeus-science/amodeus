/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;

import ch.ethz.idsc.tensor.io.Import;

public class LinkSpeedUtils {

    public static LinkSpeedDataContainer loadLinkSpeedData(File inputFile) {
        if (inputFile.isFile()){
            System.out.println("INFO loading link speed data from file " + inputFile.getAbsolutePath());
            try {
                return Import.object(inputFile);
            } catch (IOException e) {
                return null;
            } catch (ClassNotFoundException | DataFormatException e) {
                return null;
            }            
        }
        System.err.println("INFO LinkSpeedData not available");
        return null;
    }
}
