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
import org.matsim.amodeus.AmodeusConfigurator;
import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.config.modal.DispatcherConfig;
import org.matsim.amodeus.config.modal.GeneratorConfig;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.gbl.MatsimRandom;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.ReferenceFrame;
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
import ch.ethz.matsim.av.scenario.TestScenarioGenerator;

public class MATSimVirtualNetworkTravelDataTest {
    @Test
    public void testGenerateAndReuseVirtualNetworkAndTravelData() throws IOException, ClassNotFoundException, DataFormatException {
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        List<Long> virtualNetworkIds = new LinkedList<>();

        for (int i = 0; i < 2; i++) {
            Controler controler = prepare();
            controler.run();

            Assert.assertTrue(new File(workingDirectory, "generatedVirtualNetwork").exists());
            Assert.assertTrue(new File(workingDirectory, "generatedTravelData").exists());

            virtualNetworkIds.add(VirtualNetworkGet.readFile(controler.getScenario().getNetwork(), new File(workingDirectory, "generatedVirtualNetwork")).getvNetworkID());
        }

        // We want that the generated network is reused, so we expect the ID to stay the same
        Assert.assertEquals(virtualNetworkIds.get(0), virtualNetworkIds.get(1));
    }

    @Test
    public void testGenerateAndRegenerateVirtualNetworkAndTravelData() throws IOException, ClassNotFoundException, DataFormatException {
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        List<Long> virtualNetworkIds = new LinkedList<>();

        for (int i = 0; i < 2; i++) {
            Controler controler = prepare();

            AmodeusModeConfig modeConfig = AmodeusConfigGroup.get(controler.getConfig()).getMode("av");
            modeConfig.getDispatcherConfig().setRegenerateVirtualNetwork(true);
            modeConfig.getDispatcherConfig().setRegenerateTravelData(true);

            controler.run();

            Assert.assertTrue(new File(workingDirectory, "generatedVirtualNetwork").exists());
            Assert.assertTrue(new File(workingDirectory, "generatedTravelData").exists());

            virtualNetworkIds.add(VirtualNetworkGet.readFile(controler.getScenario().getNetwork(), new File(workingDirectory, "generatedVirtualNetwork")).getvNetworkID());
        }

        // The network always gets regenerated, so we expect different IDs for the two runs.
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
        Config config = ConfigUtils.createConfig(new AmodeusConfigGroup(), new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

        ScenarioOptions simOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        LocationSpec locationSpec = simOptions.getLocationSpec();
        ReferenceFrame referenceFrame = locationSpec.referenceFrame();
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(scenario.getNetwork(), referenceFrame);

        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams("av");
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        // Config

        AmodeusConfigGroup avConfig = AmodeusConfigGroup.get(config);

        AmodeusModeConfig operatorConfig = new AmodeusModeConfig("av");
        avConfig.addMode(operatorConfig);

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
        operatorConfig.getDispatcherConfig().setVirtualNetworkPath("generatedVirtualNetwork");
        operatorConfig.getDispatcherConfig().setTravelDataPath("generatedTravelData");

        // Controller
        Controler controller = new Controler(scenario);
        AmodeusConfigurator.configureController(controller, db, simOptions);

        // Run
        return controller;
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }

}
