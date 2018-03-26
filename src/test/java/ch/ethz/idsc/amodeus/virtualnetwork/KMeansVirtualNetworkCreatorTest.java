package ch.ethz.idsc.amodeus.virtualnetwork;

import static org.junit.Assert.assertEquals;

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
import ch.ethz.idsc.amodeus.prep.VirtualNetworkCreators;
import ch.ethz.idsc.amodeus.testutils.TestUtils;

public class KMeansVirtualNetworkCreatorTest {

    private static int nCreations = 5;
    private static List<VirtualNetwork<Link>> virtualNetworks = new ArrayList<>();
    private static int numVNodes;

    @BeforeClass
    public static void setup() throws IOException {

        /* input data */
        File scenarioDirectory = new File(TestUtils.getSuperFolder("amodeus"), "resources/testScenario");
        ScenarioOptions scenarioOptions = ScenarioOptions.load(scenarioDirectory);
        File configFile = new File(scenarioDirectory, scenarioOptions.getPreparerConfigName());
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath());
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();
        Population population = scenario.getPopulation();
        numVNodes = scenarioOptions.getNumVirtualNodes();
        VirtualNetworkCreators virtualNetworkCreators = scenarioOptions.getVirtualNetworkCreator();

        /* generate nCreations networks */
        for (int i = 0; i < nCreations; ++i) {
            virtualNetworks.add(virtualNetworkCreators.create(network, population, scenarioOptions));
        }

    }

    @Test
    public void testCreation() {
        // ensure that every property is the same for every combination of virtual networks
        for (int i = 0; i < virtualNetworks.size(); ++i) {
            for (int j = 0; j < virtualNetworks.size(); ++j) {

                for (int k = 0; k < numVNodes; ++k) {
                    int numLinksI = virtualNetworks.get(i).getVirtualNode(k).getLinks().size();
                    int numLinksJ = virtualNetworks.get(j).getVirtualNode(k).getLinks().size();
                    assertEquals(numLinksI, numLinksJ);
                }
                assertEquals(virtualNetworks.get(i).getvLinksCount(), virtualNetworks.get(j).getvLinksCount());
                assertEquals(virtualNetworks.get(i).getvNodesCount(), virtualNetworks.get(j).getvNodesCount());
            }
        }
    }

    @AfterClass
    public static void cleanUp() {
        // ---
    }

}
