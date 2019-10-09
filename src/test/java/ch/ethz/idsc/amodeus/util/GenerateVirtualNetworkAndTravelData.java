package ch.ethz.idsc.amodeus.util;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.matsim.StandardMATSimScenarioTest;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.test.TestFileHandling;
import ch.ethz.idsc.amodeus.traveldata.StaticTravelData;
import ch.ethz.idsc.amodeus.traveldata.StaticTravelDataCreator;
import ch.ethz.idsc.amodeus.traveldata.TravelDataIO;
import ch.ethz.idsc.amodeus.util.io.LocateUtils;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkIO;
import ch.ethz.matsim.av.config.AVConfigGroup;

public class GenerateVirtualNetworkAndTravelData {
    @BeforeClass
    public static void setUpOnce() throws IOException {
        File scenarioDirectory = new File(LocateUtils.getSuperFolder(StandardMATSimScenarioTest.class, "amodeus"), "resources/testScenario");
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        GlobalAssert.that(workingDirectory.isDirectory());
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath());
    }

    @Test
    public void generateVirtualNetworkAndTravelData() throws Exception {
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();

        Config config = ConfigUtils.loadConfig("config_full.xml", new AVConfigGroup());
        Scenario scenario = ScenarioUtils.loadScenario(config);

        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());

        int numberOfVehicles = AVConfigGroup.getOrCreate(config).getOperatorConfigs().values().iterator().next().getGeneratorConfig().getNumberOfVehicles();

        VirtualNetwork<Link> virtualNetwork = scenarioOptions.getVirtualNetworkCreator().create(scenario.getNetwork(), scenario.getPopulation(), scenarioOptions, numberOfVehicles,
                (int) config.qsim().getEndTime());

        StaticTravelData travelData = StaticTravelDataCreator.create(workingDirectory, virtualNetwork, scenario.getNetwork(), scenario.getPopulation(),
                scenarioOptions.getdtTravelData(), numberOfVehicles, (int) config.qsim().getEndTime());

        // If you want to re-generate the virtual network and travel data for the testScenario, change these paths to some path outside of the unit test space and copy
        // the files back into the testComparisonFiles folder.

        File virtualNetworkFile = new File("virtualNetwork");
        File travelDataFile = new File("travelData");
        
        // File virtualNetworkFile = new File("/home/shoerl/virtualNetwork");
        // File travelDataFile = new File("/home/shoerl/travelData");

        VirtualNetworkIO.toByte(virtualNetworkFile, virtualNetwork);
        TravelDataIO.writeStatic(travelDataFile, travelData);

        Assert.assertTrue(virtualNetworkFile.exists());
        Assert.assertTrue(travelDataFile.exists());
        
        new File("virtualNetwork").delete();
        new File("travelData").delete();
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }
}
