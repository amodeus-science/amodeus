package ch.ethz.idsc.amodeus.scenario.chicago;

import java.io.File;

import ch.ethz.idsc.amodeus.scenario.InitialNetworkPreparer;
import ch.ethz.idsc.amodeus.scenario.ScenarioAssembler;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ class ScenarioCreator {
    private final File dataDir;
    private final File taxiData;
    private final DataOperator dataOperator; // create your own as needed

    public static void main(String[] args) throws Exception {
        // customization ---
        // final File dataDir = new File("C:/Users/joelg/Documents/Studium/ETH/IDSC/TaxiData/Chicago/Chicago");
        // final File taxiData = new File("C:/Users/joelg/Documents/Studium/ETH/IDSC/TaxiData/Chicago/Taxi_Trips.csv");
        // final DataOperator dataOperator = DataOperator.CHICAGO;
        // ---
        final File dataDir = new File("C:/Users/joelg/Documents/Studium/ETH/IDSC/TaxiData/Chicago/Chicago");
        final File taxiData = ChicagoDataLoader.from("AmodeusOptions.properties", dataDir);
        final DataOperator dataOperator = DataOperator.CHICAGO_ONLINE;
        // ---
        ScenarioCreator scenarioCreator = new ScenarioCreator(dataDir, taxiData, dataOperator);
        scenarioCreator.run(new File(args[0]));
    }

    public ScenarioCreator(File dataDir, File taxiData, DataOperator dataOperator) {
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
