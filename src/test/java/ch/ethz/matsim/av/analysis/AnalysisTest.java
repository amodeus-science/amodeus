package ch.ethz.matsim.av.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.utils.io.IOUtils;

import ch.ethz.matsim.av.config.AmodeusConfigGroup;
import ch.ethz.matsim.av.config.AmodeusModeConfig;
import ch.ethz.matsim.av.config.modal.AmodeusScoringConfig;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimModule;
import ch.ethz.matsim.av.scenario.TestScenarioAnalyzer;
import ch.ethz.matsim.av.scenario.TestScenarioGenerator;

public class AnalysisTest {
    @BeforeClass
    public static void doYourOneTimeSetup() {
        new File("test_output").mkdir();
    }

    @AfterClass
    public static void doYourOneTimeTeardown() throws IOException {
        FileUtils.deleteDirectory(new File("test_output"));
    }

    @Test
    public void testAVExample() {
        AmodeusConfigGroup avConfigGroup = new AmodeusConfigGroup();

        avConfigGroup.setPassengerAnalysisInterval(2);
        avConfigGroup.setVehicleAnalysisInterval(2);

        AmodeusModeConfig operatorConfig = new AmodeusModeConfig("av");
        operatorConfig.getGeneratorConfig().setNumberOfVehicles(100);
        operatorConfig.getPricingConfig().setPricePerKm(0.48);
        operatorConfig.getPricingConfig().setSpatialBillingInterval(1000.0);
        avConfigGroup.addMode(operatorConfig);

        AmodeusScoringConfig scoringParams = operatorConfig.getScoringParameters(null);
        scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

        Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams("av");
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        config.controler().setLastIteration(2);
        config.controler().setWriteEventsInterval(1);

        StrategySettings strategySettings = new StrategySettings();
        strategySettings.setStrategyName("KeepLastSelected");
        strategySettings.setWeight(1.0);
        config.strategy().addStrategySettings(strategySettings);

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new DvrpModule());
        controler.addOverridingModule(new AVModule());
        controler.addOverridingQSimModule(new AVQSimModule());

        controler.configureQSimComponents(AVQSimModule.activateModes(avConfigGroup));

        TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
        controler.addOverridingModule(analyzer);

        controler.run();

        Assert.assertEquals(101, countLines("test_output/output/ITERS/it.0/0.amodeus_passenger_rides.csv"));
        Assert.assertEquals(501, countLines("test_output/output/ITERS/it.0/0.amodeus_vehicle_activities.csv"));
        Assert.assertEquals(201, countLines("test_output/output/ITERS/it.0/0.amodeus_vehicle_movements.csv"));

        Assert.assertEquals(101, countLines("test_output/output/ITERS/it.1/1.amodeus_passenger_rides.csv"));
        Assert.assertEquals(501, countLines("test_output/output/ITERS/it.1/1.amodeus_vehicle_activities.csv"));
        Assert.assertEquals(201, countLines("test_output/output/ITERS/it.1/1.amodeus_vehicle_movements.csv"));

        Assert.assertEquals(101, countLines("test_output/output/ITERS/it.2/2.amodeus_passenger_rides.csv"));
        Assert.assertEquals(501, countLines("test_output/output/ITERS/it.2/2.amodeus_vehicle_activities.csv"));
        Assert.assertEquals(201, countLines("test_output/output/ITERS/it.2/2.amodeus_vehicle_movements.csv"));
    }

    private int countLines(String path) {
        int numberOfLines = 0;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));

            while (reader.readLine() != null) {
                numberOfLines++;
            }

            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return numberOfLines;
    }

    @SuppressWarnings("resource")
    @Test
    public void testReproducebility() throws FileNotFoundException, IOException {
        runRepro("output1");
        runRepro("output2");

        safeFileAssert("test_output/output1/ITERS/it.0/0.events.xml.gz", "test_output/output2/ITERS/it.0/0.events.xml.gz");
        safeFileAssert("test_output/output1/ITERS/it.2/2.events.xml.gz", "test_output/output2/ITERS/it.2/2.events.xml.gz");
        safeFileAssert("test_output/output1/ITERS/it.0/0.amodeus_passenger_rides.csv", "test_output/output2/ITERS/it.0/0.amodeus_passenger_rides.csv");
        safeFileAssert("test_output/output1/ITERS/it.2/2.amodeus_passenger_rides.csv", "test_output/output2/ITERS/it.2/2.amodeus_passenger_rides.csv");
        safeFileAssert("test_output/output1/ITERS/it.0/0.amodeus_vehicle_movements.csv", "test_output/output2/ITERS/it.0/0.amodeus_vehicle_movements.csv");
        safeFileAssert("test_output/output1/ITERS/it.2/2.amodeus_vehicle_movements.csv", "test_output/output2/ITERS/it.2/2.amodeus_vehicle_movements.csv");
        safeFileAssert("test_output/output1/ITERS/it.0/0.amodeus_vehicle_activities.csv", "test_output/output2/ITERS/it.0/0.amodeus_vehicle_activities.csv");
        safeFileAssert("test_output/output1/ITERS/it.2/2.amodeus_vehicle_activities.csv", "test_output/output2/ITERS/it.2/2.amodeus_vehicle_activities.csv");
    }

    private void safeFileAssert(String pathA, String pathB) throws IOException {
        try (FileInputStream fia = new FileInputStream(new File(pathA)); FileInputStream fib = new FileInputStream(new File(pathB))) {
            Assert.assertTrue(IOUtils.isEqual(fia, fib));
        }
    }

    private void runRepro(String path) {
        AmodeusConfigGroup avConfigGroup = new AmodeusConfigGroup();

        avConfigGroup.setPassengerAnalysisInterval(2);
        avConfigGroup.setVehicleAnalysisInterval(2);

        AmodeusModeConfig operatorConfig = new AmodeusModeConfig("av");
        operatorConfig.getGeneratorConfig().setNumberOfVehicles(100);
        operatorConfig.getPricingConfig().setPricePerKm(0.48);
        operatorConfig.getPricingConfig().setSpatialBillingInterval(1000.0);
        avConfigGroup.addMode(operatorConfig);

        AmodeusScoringConfig scoringParams = operatorConfig.getScoringParameters(null);
        scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

        Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams("av");
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        config.controler().setLastIteration(2);
        config.controler().setWriteEventsInterval(1);
        config.controler().setOutputDirectory("test_output/" + path);

        StrategySettings strategySettings = new StrategySettings();
        strategySettings.setStrategyName("KeepLastSelected");
        strategySettings.setWeight(1.0);
        config.strategy().addStrategySettings(strategySettings);

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new DvrpModule());
        controler.addOverridingModule(new AVModule());
        controler.addOverridingQSimModule(new AVQSimModule());

        controler.configureQSimComponents(AVQSimModule.activateModes(avConfigGroup));

        TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
        controler.addOverridingModule(analyzer);

        controler.run();

        Assert.assertEquals(101, countLines("test_output/" + path + "/ITERS/it.0/0.amodeus_passenger_rides.csv"));
        Assert.assertEquals(501, countLines("test_output/" + path + "/ITERS/it.0/0.amodeus_vehicle_activities.csv"));
        Assert.assertEquals(201, countLines("test_output/" + path + "/ITERS/it.0/0.amodeus_vehicle_movements.csv"));

        Assert.assertEquals(101, countLines("test_output/" + path + "/ITERS/it.1/1.amodeus_passenger_rides.csv"));
        Assert.assertEquals(501, countLines("test_output/" + path + "/ITERS/it.1/1.amodeus_vehicle_activities.csv"));
        Assert.assertEquals(201, countLines("test_output/" + path + "/ITERS/it.1/1.amodeus_vehicle_movements.csv"));

        Assert.assertEquals(101, countLines("test_output/" + path + "/ITERS/it.2/2.amodeus_passenger_rides.csv"));
        Assert.assertEquals(501, countLines("test_output/" + path + "/ITERS/it.2/2.amodeus_vehicle_activities.csv"));
        Assert.assertEquals(201, countLines("test_output/" + path + "/ITERS/it.2/2.amodeus_vehicle_movements.csv"));
    }
}
