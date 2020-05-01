/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.DataFormatException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.gbl.MatsimRandom;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDatabaseModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDispatcherModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusVehicleGeneratorModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusVehicleToVSGeneratorModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusVirtualNetworkModule;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.LPOptions;
import ch.ethz.idsc.amodeus.options.LPOptionsBase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.test.TestFileHandling;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkGet;
import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.DispatcherConfig;
import ch.ethz.matsim.av.config.operator.GeneratorConfig;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimModule;
import ch.ethz.matsim.av.scenario.TestScenarioGenerator;
import ch.ethz.refactoring.AmodeusConfigurator;

public class MATSimVirtualNetworkTravelDataTest {
    @Test
    public void testGenerateAndReuseVirtualNetworkAndTravelData() throws IOException, ClassNotFoundException, DataFormatException {
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        List<Long> virtualNetworkIds = new LinkedList<>();

        for (int i = 0; i < 2; i++) {
            Controler controler = prepare();

            AVConfigGroup.getOrCreate(controler.getConfig()).getOperatorConfigs().values().iterator().next().getParams().put("regenerateVirtualNetwork", "false");
            AVConfigGroup.getOrCreate(controler.getConfig()).getOperatorConfigs().values().iterator().next().getParams().put("regenerateTravelData", "false");

            controler.run();

            Assert.assertTrue(new File(workingDirectory, "generatedVirtualNetwork").exists());
            Assert.assertTrue(new File(workingDirectory, "generatedTravelData").exists());

            virtualNetworkIds.add(VirtualNetworkGet.readFile(controler.getScenario().getNetwork(), new File(workingDirectory, "generatedVirtualNetwork")).getvNetworkID());
        }

        // Here, we want that the generated network is reused, so we expect the ID to stay the same
        Assert.assertEquals(virtualNetworkIds.get(0), virtualNetworkIds.get(1));
    }

    @Test
    public void testGenerateAndRegenerateVirtualNetworkAndTravelData() throws IOException, ClassNotFoundException, DataFormatException {
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        List<Long> virtualNetworkIds = new LinkedList<>();

        for (int i = 0; i < 2; i++) {
            Controler controler = prepare();
            controler.run();

            Assert.assertTrue(new File(workingDirectory, "generatedVirtualNetwork").exists());
            Assert.assertTrue(new File(workingDirectory, "generatedTravelData").exists());

            virtualNetworkIds.add(VirtualNetworkGet.readFile(controler.getScenario().getNetwork(), new File(workingDirectory, "generatedVirtualNetwork")).getvNetworkID());
        }

        // By default, the network always gets regenerated, so we expect different IDs for the two runs.
        Assert.assertNotEquals(virtualNetworkIds.get(0), virtualNetworkIds.get(1));
    }

    private static Controler prepare() throws IOException {
        File scenarioDirectory = new File(Locate.repoFolder(StandardMATSimScenarioTest.class, "amodeus"), "resources/testScenario");
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        GlobalAssert.that(workingDirectory.isDirectory());
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath());

        StaticHelper.setup();
        MatsimRandom.reset();

        // Set up
        Config config = ConfigUtils.createConfig(new AVConfigGroup(), new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

        ScenarioOptions simOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        LocationSpec locationSpec = simOptions.getLocationSpec();
        ReferenceFrame referenceFrame = locationSpec.referenceFrame();
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(scenario.getNetwork(), referenceFrame);

        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams(AVModule.AV_MODE);
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        Controler controller = new Controler(scenario);
        AmodeusConfigurator.configureController(controller, db, simOptions);

        // Config

        AVConfigGroup avConfig = AVConfigGroup.getOrCreate(config);
        avConfig.setAllowedLinkMode("car");

        OperatorConfig operatorConfig = new OperatorConfig();
        operatorConfig.setId(AVOperator.createId("test"));
        avConfig.addOperator(operatorConfig);

        GeneratorConfig generatorConfig = operatorConfig.getGeneratorConfig();
        generatorConfig.setType("VehicleToVSGenerator");
        generatorConfig.setNumberOfVehicles(100);

        // Choose a dispatcher
        DispatcherConfig dispatcherConfig = operatorConfig.getDispatcherConfig();
        dispatcherConfig.addParam("infoLinePeriod", "3600");
        dispatcherConfig.setType("FeedforwardFluidicRebalancingPolicy");

        // Make sure that we do not need the SimulationObjectCompiler
        dispatcherConfig.addParam("publishPeriod", "-1");

        // Set up stuff for TravelData (but we'll generate it on the fly)
        LPOptions lpOptions = new LPOptions(simOptions.getWorkingDirectory(), LPOptionsBase.getDefault());
        lpOptions.setProperty(LPOptionsBase.LPSOLVER, "timeInvariant");
        lpOptions.saveAndOverwriteLPOptions();

        // Set up paths
        operatorConfig.getParams().put("virtualNetworkPath", "generatedVirtualNetwork");
        operatorConfig.getParams().put("travelDataPath", "generatedTravelData");

        // Run
        return controller;
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }

}
