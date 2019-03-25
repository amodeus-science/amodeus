/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import ch.ethz.idsc.amodeus.util.io.CopySomeFiles;
import ch.ethz.idsc.amodeus.util.io.FileDelete;

public enum TestFileHandling {
    ;

    // TODO cleanup
    public static void copyScnearioToMainDirectory(String scenarioDir, String mainDir) throws IOException {

        CopySomeFiles.now(scenarioDir, mainDir, //
                Arrays.asList(new String[] { "AmodeusOptions.properties", "av.xml", //
                        "av_v1.dtd", "config_full.xml", "linkSpeedData.bin", //
                        "network.xml", "population.xml" }),
                true);
    }

    public static void removeGeneratedFiles() throws IOException {

        /** single files */
        Collection<File> singleFiles = new ArrayList<>();
        singleFiles.add(new File("av.xml"));
        singleFiles.add(new File("av_v1.dtd"));
        singleFiles.add(new File("config.xml"));
        singleFiles.add(new File("config_full.xml"));
        singleFiles.add(new File("linkSpeedData.bin"));
        singleFiles.add(new File("preparedNetwork.xml"));
        singleFiles.add(new File("preparedNetwork.xml.gz"));
        singleFiles.add(new File("preparedPopulation.xml"));
        singleFiles.add(new File("preparedPopulation.xml.gz"));
        singleFiles.add(new File("network.xml"));
        singleFiles.add(new File("population.xml"));
        singleFiles.add(new File("AmodeusOptions.properties"));
        singleFiles.add(new File("LPOptions.properties"));

        for (File file : singleFiles) {
            if (file.exists())
                FileDelete.of(file, 0, 1);
        }

        /** virtual network folder */
        File virtualNetwork = new File("virtualNetwork");
        if (virtualNetwork.exists())
            FileDelete.of(virtualNetwork, 1, 4);

        /** output folder */
        File output = new File("output");
        if (output.exists())
            FileDelete.of(output, 5, 15700);

    }

}
