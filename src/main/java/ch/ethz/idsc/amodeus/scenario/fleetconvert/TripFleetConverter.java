package ch.ethz.idsc.amodeus.scenario.fleetconvert;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.scenario.DataOperator;
import ch.ethz.idsc.amodeus.scenario.population.TripPopulationCreator;
import ch.ethz.idsc.amodeus.util.io.FileDelete;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class TripFleetConverter implements FleetConverter {

    @Override
    public void run(File processingDir, File tripFile, DataOperator dataOperator) throws Exception {
        GlobalAssert.that(tripFile.isFile());

        // Prepare Environment and load all configuration files
        // ===================================
        ScenarioOptions simOptions = new ScenarioOptions(processingDir, ScenarioOptionsBase.getDefault());

        File configFile = new File(processingDir, simOptions.getPreparerConfigName());
        GlobalAssert.that(configFile.exists());
        Config configFull = ConfigUtils.loadConfig(configFile.toString());
        Network network = NetworkLoader.fromNetworkFile(new File(processingDir, configFull.network().getInputFile())); // loadNetwork(configFile);
        GlobalAssert.that(!network.getNodes().isEmpty());

        System.out.println("INFO working folder: " + processingDir.getAbsolutePath());
        ReferenceFrame referenceFrame = simOptions.getLocationSpec().referenceFrame();
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, referenceFrame);

        File outputDirectory = new File(processingDir, configFull.controler().getOutputDirectory());

        System.err.println(outputDirectory.getAbsolutePath());
        if (processingDir.exists()) {
            if (outputDirectory.exists()) {
                System.err.println("WARN All files in the that folder will be deleted in:");
                for (int i = 2; i > 0; i--) {
                    Thread.sleep(1000);
                    System.err.println(i + " seconds");
                }
                FileDelete.of(outputDirectory, 2, 10);
            }
            outputDirectory.mkdirs();

        }

        // New folder with tripData
        // ===================================
        File newWorkingDir = new File(processingDir, "tripData");
        newWorkingDir.mkdirs();
        FileUtils.copyFileToDirectory(tripFile, newWorkingDir);
        File newTripFile = new File(newWorkingDir, tripFile.getName());
        GlobalAssert.that(newTripFile.isFile());

        // Data correction SCENARIO SPECIFIC
        // ===================================
        File correctedTripFile = dataOperator.dataCorrector.correctFile(newTripFile, db);
        GlobalAssert.that(correctedTripFile.isFile());

        // Data cleansing
        // ===================================
        File cleanTripFile = dataOperator.cleaner.clean(correctedTripFile, simOptions, network);
        GlobalAssert.that(cleanTripFile.isFile());

        // Create Population
        // ===================================
        TripPopulationCreator populationCreator = new TripPopulationCreator(processingDir, configFull, network, db, //
                dataOperator.cleaner.dateFormat);
        populationCreator.process(cleanTripFile);
    }
}
