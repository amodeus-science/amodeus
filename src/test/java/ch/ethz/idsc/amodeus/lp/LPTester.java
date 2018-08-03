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
import ch.ethz.idsc.amodeus.testutils.TestUtils;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;

public class LPTester {
    private static VirtualNetwork<Link> virtualNetwork2;
    private static VirtualNetwork<Link> virtualNetwork3;
    private static VirtualNetwork<Link> virtualNetwork3incomplete;
    private static int numVNodes;
    private static ScenarioOptions scenarioOptions;
    private static Population population;
    private static Network network;

    @BeforeClass
    public static void setup() throws IOException {

        /* input data */
        File scenarioDirectory = new File(TestUtils.getSuperFolder("amodeus"), "resources/testScenario");
        scenarioOptions = new ScenarioOptions(scenarioDirectory, ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioDirectory, scenarioOptions.getPreparerConfigName());
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath());
        Scenario scenario = ScenarioUtils.loadScenario(config);
        network = scenario.getNetwork();
        population = scenario.getPopulation();

        // create 2 node virtual network
        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "2");
        VirtualNetworkCreator virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();
        virtualNetwork2 = virtualNetworkCreator.create(network, population);

        // create 3 node virtual network
        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "3");
        virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();
        virtualNetwork3 = virtualNetworkCreator.create(network, population);

        // create 3 node virtual network incomplete
        scenarioOptions.setProperty(ScenarioOptionsBase.COMPLETEGRAPHIDENTIFIER, "false");
        virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();
        virtualNetwork3incomplete = virtualNetworkCreator.create(network, population);
    }

    @Test
    public void testCreation() {
        System.out.println(virtualNetwork2.getvLinksCount());
        assertEquals(virtualNetwork2.getvLinksCount(), 2);
        assertEquals(virtualNetwork2.getvNodesCount(), 2);

        assertEquals(virtualNetwork3.getvLinksCount(), 6);
        assertEquals(virtualNetwork3.getvNodesCount(), 3);

        assertEquals(virtualNetwork3incomplete.getvLinksCount(), 4);
        assertEquals(virtualNetwork3incomplete.getvNodesCount(), 3);
    }

    @AfterClass
    public static void cleanUp() {
        // ---
    }

}
