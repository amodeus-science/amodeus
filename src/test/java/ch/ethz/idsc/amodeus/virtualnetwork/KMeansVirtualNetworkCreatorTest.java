/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.modal.GeneratorConfig;
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
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;

public class KMeansVirtualNetworkCreatorTest {

    private static int nCreations = 7;
    private static List<VirtualNetwork<Link>> virtualNetworks = new ArrayList<>();
    private static int numVNodes;

    @BeforeClass
    public static void setup() throws IOException {

        /* input data */
        File scenarioDirectory = new File(Locate.repoFolder(KMeansVirtualNetworkCreatorTest.class, "amodeus"), "resources/testScenario");
        ScenarioOptions scenarioOptions = new ScenarioOptions(scenarioDirectory, ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioOptions.getPreparerConfigName());

        AmodeusConfigGroup avCg = new AmodeusConfigGroup();
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath(), avCg);
        GeneratorConfig genConfig = avCg.getModes().values().iterator().next().getGeneratorConfig();
        int numRt = genConfig.getNumberOfVehicles();
        int endTime = (int) config.qsim().getEndTime().seconds();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();
        Population population = scenario.getPopulation();
        numVNodes = scenarioOptions.getNumVirtualNodes();
        VirtualNetworkCreator virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();

        /* generate nCreations networks */
        for (int i = 0; i < nCreations; ++i)
            virtualNetworks.add(virtualNetworkCreator.create(network, population, scenarioOptions, numRt, endTime));
    }

    @Test
    public void testCreation() {
        // ensure that every property is the same for every combination of virtual networks
        for (VirtualNetwork<Link> virtualNetworkI : virtualNetworks)
            for (VirtualNetwork<Link> virtualNetworkJ : virtualNetworks) {
                for (int k = 0; k < numVNodes; ++k) {
                    int numLinksI = virtualNetworkI.getVirtualNode(k).getLinks().size();
                    int numLinksJ = virtualNetworkJ.getVirtualNode(k).getLinks().size();
                    assertEquals(numLinksI, numLinksJ);
                }
                assertEquals(virtualNetworkI.getvLinksCount(), virtualNetworkJ.getvLinksCount());
                assertEquals(virtualNetworkI.getvNodesCount(), virtualNetworkJ.getvNodesCount());
            }
    }
}
