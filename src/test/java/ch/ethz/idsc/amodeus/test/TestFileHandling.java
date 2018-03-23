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

    public static void removeGeneratedFiles(File workingDirectory) throws IOException {

        /** single files */
        Collection<File> singleFiles = new ArrayList<>();
        singleFiles.add(new File(workingDirectory, "av.xml"));
        singleFiles.add(new File(workingDirectory, "config.xml"));
        singleFiles.add(new File(workingDirectory, "config_full.xml"));
        singleFiles.add(new File(workingDirectory, "linkSpeedData.bin"));
        singleFiles.add(new File(workingDirectory, "preparedNetwork.xml"));
        singleFiles.add(new File(workingDirectory, "preparedNetwork.xml.gz"));
        singleFiles.add(new File(workingDirectory, "preparedPopulation.xml"));
        singleFiles.add(new File(workingDirectory, "preparedPopulation.xml.gz"));
        singleFiles.add(new File(workingDirectory, "network.xml"));
        singleFiles.add(new File(workingDirectory, "population.xml"));
        singleFiles.add(new File(workingDirectory, "AmodeusOptions.properties"));

        for (File file : singleFiles) {
            FileDelete.of(file, 0, 1);
        }

        /** virtual network folder */
        FileDelete.of(new File(workingDirectory, "virtualNetwork"), 1, 4);

        /** output folder */
        FileDelete.of(new File(workingDirectory, "output"), 5, 10999);

    }

}
