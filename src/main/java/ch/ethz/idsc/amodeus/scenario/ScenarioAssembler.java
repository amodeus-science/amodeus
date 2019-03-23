/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario;

import ch.ethz.idsc.amodeus.util.io.FileDelete;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public enum ScenarioAssembler {
    ;

    public static void copyInitialFiles(File processingDirectory, File dataDir) throws Exception {

        System.out.println("copying data files from : " + dataDir);

        /** empty the processing folder */
        System.err.println(processingDirectory.getAbsolutePath());
        if (processingDirectory.exists()) {
            System.err.println("WARN All files in the that folder will be deleted in:");
            for (int i = 2; i > 0; i--) {
                Thread.sleep(1000);
                System.err.println(i + " seconds");
            }
            FileDelete.of(processingDirectory, 2, 14);
            processingDirectory.mkdir();
        } else
            GlobalAssert.that(processingDirectory.mkdir());

        /** copy initial config files */
        CopyOption[] options = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };
        String[] fileNames = new String[] { "AmodeusOptions.properties", "av.xml", //
                "config_full.xml", "config.xml", "network.xml" };

        for (String fileName : fileNames) {
            File sourceFile = new File(dataDir, fileName);
            if (sourceFile.exists()) {
                System.out.println("sourceFile: " + sourceFile.getAbsolutePath());

                Path source = Paths.get(sourceFile.getAbsolutePath());
                Path target = Paths.get(processingDirectory.getPath(), fileName);
                Files.copy(source, target, options);
            } else
                new IOException(sourceFile.getAbsolutePath() + " does not exist!").printStackTrace();
        }
    }

    public static void copyFinishedScenario(String originDir, String destinDir) throws IOException {
        System.out.println("copying scenario from : " + originDir);
        System.out.println("copying  scenario  to : " + destinDir);

        File destinDirFile = new File(destinDir);

        if (destinDirFile.exists()) {
            FileDelete.of(destinDirFile, 2, 10);
        }
        destinDirFile.mkdir();

        @SuppressWarnings("unused")
        CopyOption[] options = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };

        String[] fileNames = new String[] { "av.xml", "AmodeusOptions.properties", "network.xml.gz", "population.xml.gz", "config_full.xml" };

        for (String fileName : fileNames) {
            Path source = Paths.get(originDir, fileName);
            Path target = Paths.get(destinDir, fileName);
            try {
                Files.copy(source, target /* , options */);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
