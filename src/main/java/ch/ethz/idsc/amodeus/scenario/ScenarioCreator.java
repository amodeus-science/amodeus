/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario;

import java.io.File;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class ScenarioCreator {
    private final File dataDir;
    private final File taxiData;
    private final DataOperator<?> dataOperator; // create your own as needed

    public ScenarioCreator(File dataDir, File taxiData, DataOperator<?> dataOperator) {
        GlobalAssert.that(dataDir.isDirectory());
        GlobalAssert.that(taxiData.exists());
        this.dataDir = dataDir;
        this.taxiData = taxiData;
        this.dataOperator = dataOperator;
    }

    public void run(File workingDirectory) throws Exception {
        System.out.println(workingDirectory);
        final File destinDir = new File(workingDirectory, "CreatedScenario");
        final File processingDir = new File(workingDirectory, "Scenario");
        ScenarioAssembler.copyInitialFiles(processingDir, dataDir);
        InitialNetworkPreparer.run(processingDir);
        dataOperator.setFilters();
        dataOperator.fleetConverter.run(processingDir, taxiData, dataOperator);
        ScenarioAssembler.copyFinishedScenario(processingDir.getAbsolutePath(), destinDir.getAbsolutePath());
    }
}
