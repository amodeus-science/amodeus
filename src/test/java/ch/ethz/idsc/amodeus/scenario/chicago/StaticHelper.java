package ch.ethz.idsc.amodeus.scenario.chicago;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import ch.ethz.idsc.amodeus.scenario.Pt2MatsimXML;
import ch.ethz.idsc.amodeus.scenario.ScenarioLabels;
import ch.ethz.idsc.amodeus.util.io.CopySomeFiles;
import ch.ethz.idsc.amodeus.util.io.FileDelete;
import ch.ethz.idsc.amodeus.util.io.LocateUtils;

/* package */ class StaticHelper {
    ;

    public static void setupTest(File workingDir) throws Exception {
        ChicagoGeoInformation.setup();
        /** copy relevant files containing settings for scenario generation */
        File settingsDir = new File(LocateUtils.getSuperFolder("amodeus"), "resources/chicagoScenarioTest");
        CopySomeFiles.now(settingsDir.getAbsolutePath(), workingDir.getAbsolutePath(), //
                Arrays.asList(new String[] { ScenarioLabels.avFile, ScenarioLabels.config, //
                        ScenarioLabels.pt2MatSettings }),
                true);
        /** AmodeusOptions.properties is not replaced as it might be changed by user during
         * scenario generation process. */
        if (!new File(workingDir, ScenarioLabels.amodeusFile).exists())
            CopySomeFiles.now(settingsDir.getAbsolutePath(), workingDir.getAbsolutePath(), //
                    Arrays.asList(new String[] { ScenarioLabels.amodeusFile }), false);
        Pt2MatsimXML.toLocalFileSystem(new File(workingDir, ScenarioLabels.pt2MatSettings), //
                workingDir.getAbsolutePath());
    }

    public static void cleanUpTest(File workingDir) throws IOException {
        /** delete unneeded files */
        FileDelete.of(new File(workingDir, "Scenario"), 2, 14);
        FileDelete.of(new File(workingDir, ScenarioLabels.amodeusFile), 0, 1);
        FileDelete.of(new File(workingDir, ScenarioLabels.avFile), 0, 1);
        FileDelete.of(new File(workingDir, ScenarioLabels.config), 0, 1);
        FileDelete.of(new File(workingDir, ScenarioLabels.pt2MatSettings), 0, 1);
        FileDelete.of(new File(workingDir, ScenarioLabels.network), 0, 1);
        FileDelete.of(new File(workingDir, ScenarioLabels.osmData), 0, 1);
        FileDelete.of(new File(workingDir, "Taxi_Trips_2014_11_18.csv"), 0, 1);
        FileDelete.of(new File(workingDir, "CreatedScenario"), 1, 6);
    }
}
