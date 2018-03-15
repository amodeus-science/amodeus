/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;

import ch.ethz.idsc.amodeus.util.io.FileDelete;

/* package */ enum TestFileHandling {
    ;

    public static void copyScnearioToMainDirectory(String scenarioDir, String mainDir) throws IOException {

        CopyOption[] options = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };

        String[] fileNames = new String[] { "AmodeusOptions.properties", "av.xml", "config_full.xml", //
                "linkSpeedData.bin", "network.xml", "population.xml" };
        for (String fileName : fileNames) {
            Path source = Paths.get(scenarioDir, fileName);
            Path target = Paths.get(mainDir, fileName);
            Files.copy(source, target, options);
        }

    }

    public static void removeGeneratedFiles() throws IOException {

        /** single files */
        Collection<File> singleFiles = new ArrayList<>();
        singleFiles.add(new File("av.xml"));
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

        for (File file : singleFiles) {
            FileDelete.of(file, 0, 1);
        }

        /** virtual network folder */
        FileDelete.of(new File("virtualNetwork"), 1, 4);

        /** output folder */
        FileDelete.of(new File("output"), 5, 10999);

    }

}
