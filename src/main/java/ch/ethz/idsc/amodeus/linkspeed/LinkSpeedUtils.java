/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.io.File;

import ch.ethz.idsc.tensor.io.Import;

public enum LinkSpeedUtils {
    ;

    public static LinkSpeedDataContainer loadLinkSpeedData(File inputFile) {
        if (inputFile.isFile()) {
            System.out.println("INFO loading link speed data from file " + inputFile.getAbsolutePath());
            try {
                return Import.object(inputFile);
            } catch (Exception exception) {
                exception.printStackTrace();
                return null;
            }
        }
        System.err.println("INFO LinkSpeedData not available");
        return null;
    }
}
