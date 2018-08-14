package ch.ethz.idsc.amodeus.virtualnetwork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class KMeansCascadeVirtualNetworkCreatorTest {

    private static int nCreations = 5;
    private static List<VirtualNetwork<Link>> virtualNetworks = new ArrayList<>();
    private static List<VirtualNetwork<Link>> virtualNetworksValid = new ArrayList<>();
    private static List<VirtualNetwork<Link>> virtualNetworksInvalid = new ArrayList<>();
    private static int numVNodes;
    private static ScenarioOptions scenarioOptions;
    private static Population population;
    private static Network network;

    @BeforeClass
    public static void setup() throws IOException {

        /* input data */
        File scenarioDirectory = new File(TestUtils.getSuperFolder("amodeus"), "resources/testScenario");
        scenarioOptions = new ScenarioOptions(scenarioDirectory, ScenarioOptionsBase.getDefault());
        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "16");
        scenarioOptions.setProperty(ScenarioOptionsBase.VIRTUALNETWORKCREATORIDENTIFIER, "KMEANSCASCADE");
        File configFile = new File(scenarioDirectory, scenarioOptions.getPreparerConfigName());
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath());
        Scenario scenario = ScenarioUtils.loadScenario(config);
        network = scenario.getNetwork();
        population = scenario.getPopulation();
        numVNodes = scenarioOptions.getNumVirtualNodes();
        VirtualNetworkCreator virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();

        /* generate nCreations networks */
        for (int i = 0; i < nCreations; ++i) {
            virtualNetworks.add(virtualNetworkCreator.create(network, population));
        }
    }

    @Test
    public void testCreation() {
        // ensure that every property is the same for every combination of virtual
        // networks
        for (int i = 0; i < virtualNetworks.size() - 1; ++i) {

            for (int k = 0; k < numVNodes; ++k) {
                int numLinksI = virtualNetworks.get(i).getVirtualNode(k).getLinks().size();
                int numLinksJ = virtualNetworks.get(i + 1).getVirtualNode(k).getLinks().size();
                assertEquals(numLinksI, numLinksJ);
            }
            assertEquals(virtualNetworks.get(i).getvLinksCount(), virtualNetworks.get(i + 1).getvLinksCount());
            assertEquals(virtualNetworks.get(i).getvNodesCount(), virtualNetworks.get(i + 1).getvNodesCount());
        }

    }

    @Test
    public void testOrderedIndices() {
        // ensure that the received virtualNetworks contain a ordered list of the virtual nodes in terms of indices
        for (VirtualNetwork<Link> virtualNetwork : virtualNetworks) {
            int index = 0;
            for (VirtualNode<Link> node : virtualNetwork.getVirtualNodes()) {
                assertEquals(node.getIndex(), index++);
                assertEquals(node.getId(), "vNode_" + index);
            }
        }

    }

    @Test
    public void testDifferentVirtualNodeNumbers() {
        VirtualNetworkCreator virtualNetworkCreator;

        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "2");
        virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();

        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "4");
        virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();

        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "8");
        virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();
        virtualNetworksValid.add(virtualNetworkCreator.create(network, population));

        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "1");
        virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();
        try {
            virtualNetworksInvalid.add(virtualNetworkCreator.create(network, population));
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }

        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "10");
        virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();
        try {
            virtualNetworksInvalid.add(virtualNetworkCreator.create(network, population));
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }
    }

    @AfterClass
    public static void cleanUp() {
        // ---
    }

}
