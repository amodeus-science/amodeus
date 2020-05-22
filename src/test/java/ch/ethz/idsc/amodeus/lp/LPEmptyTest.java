/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkCreator;
import ch.ethz.idsc.amodeus.test.TestFileHandling;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.matsim.av.config.AmodeusConfigGroup;
import ch.ethz.matsim.av.config.modal.GeneratorConfig;

public class LPEmptyTest {
    private static VirtualNetwork<Link> virtualNetwork2;
    private static VirtualNetwork<Link> virtualNetwork3;
    private static ScenarioOptions scenarioOptions;
    private static Population population;
    private static Network network;
    private static int endTime;

    @BeforeClass
    public static void setUp() throws IOException {
        System.out.println(LPTimeInvariant.class.getName());
        // copy scenario data into main directory
        File scenarioDirectory = new File(Locate.repoFolder(LPEmptyTest.class, "amodeus"), "resources/testScenario");
        System.out.println("scenarioDirectory: " + scenarioDirectory);
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        GlobalAssert.that(workingDirectory.isDirectory());
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath());

        /* input data */
        scenarioOptions = new ScenarioOptions(scenarioDirectory, ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        System.out.println("configFile: " + configFile.getAbsolutePath());
        AmodeusConfigGroup avCg = new AmodeusConfigGroup();
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath(), avCg);
        GeneratorConfig genConfig = avCg.getModes().values().iterator().next().getGeneratorConfig();
        int numRt = genConfig.getNumberOfVehicles();
        endTime = (int) config.qsim().getEndTime().seconds();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        network = scenario.getNetwork();
        population = scenario.getPopulation();

        // create 2 node virtual network
        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "2");
        VirtualNetworkCreator virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();
        virtualNetwork2 = virtualNetworkCreator.create(network, population, scenarioOptions, numRt, endTime);

        // create 3 node virtual network
        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "3");
        virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();
        virtualNetwork3 = virtualNetworkCreator.create(network, population, scenarioOptions, numRt, endTime);
    }

    @Test
    public void testCreation() {
        assertEquals(virtualNetwork2.getvLinksCount(), 2);
        assertEquals(virtualNetwork2.getvNodesCount(), 2);

        assertEquals(virtualNetwork3.getvLinksCount(), 6);
        assertEquals(virtualNetwork3.getvNodesCount(), 3);
    }

    @Test
    public void testLP2Nodes() {
        // init LP empty
        LPEmpty lp = new LPEmpty(virtualNetwork2, Tensors.of(Array.zeros(2, 2)), endTime);
        assertEquals(lp.getTimeIntervalLength(), 30 * 3600); // there is only one time interval over the whole day

        lp.initiateLP();
        lp.solveLP(false);
        assertEquals(Tensors.of(Array.zeros(2, 2)), lp.getAlphaAbsolute_ij());
        assertEquals(Tensors.of(Array.zeros(2, 2)), lp.getAlphaRate_ij());
        assertEquals(Tensors.of(Array.zeros(2, 2)), lp.getFAbsolute_ij());
        assertEquals(Tensors.of(Array.zeros(2, 2)), lp.getFRate_ij());
        assertEquals(Array.zeros(2), lp.getV0_i());
    }

    @Test
    public void testLP3Nodes() {
        // init LP empty
        LPEmpty lp = new LPEmpty(virtualNetwork3, Tensors.of(Array.zeros(3, 3)), endTime);
        assertEquals(lp.getTimeIntervalLength(), 30 * 3600); // there is only one time interval in [0, endTime]

        lp.initiateLP();
        lp.solveLP(false);
        assertEquals(Tensors.of(Array.zeros(3, 3)), lp.getAlphaAbsolute_ij());
        assertEquals(Tensors.of(Array.zeros(3, 3)), lp.getAlphaRate_ij());
        assertEquals(Tensors.of(Array.zeros(3, 3)), lp.getFAbsolute_ij());
        assertEquals(Tensors.of(Array.zeros(3, 3)), lp.getFRate_ij());
        assertEquals(Array.zeros(3), lp.getV0_i());
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }
}
