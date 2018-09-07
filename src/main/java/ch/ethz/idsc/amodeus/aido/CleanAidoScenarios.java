/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.io.File;

public enum CleanAidoScenarios {
    ;

    /** clean up downloaded files for aido scenaros */
    public static void now() {
        String[] files = new String[] { "AmodeusOptions.properties", "av.xml", //
                "matsimConfig.xml", "personAtrributes-with-subpopulation.xml", //
                "preparedNetwork.xml", "config_full.xml", "linkSpeedData", //
                "config.xml", "preparedConfig.xml", "preparedPopulation.xml", //
                "population.xml" };
        for (String file : files) {
            boolean ok = new File(file).delete();
            if (!ok) {
                System.out.println("file: " + file + " not deleted, possibly not created / downloaded.");
            }
        }
    }
}
