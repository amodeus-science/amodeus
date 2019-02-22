/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import ch.ethz.idsc.amodeus.util.io.ProvideAVConfig;
import ch.ethz.idsc.amodeus.util.io.LocateUtils;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.framework.AVConfigGroup;

public class KMeansVirtualNetworkCreatorTest {

    private static int nCreations = 20;
    private static List<VirtualNetwork<Link>> virtualNetworks = new ArrayList<>();
    private static int numVNodes;

    @BeforeClass
    public static void setup() throws IOException {

        /* input data */
        File scenarioDirectory = new File(LocateUtils.getSuperFolder("amodeus"), "resources/testScenario");
        ScenarioOptions scenarioOptions = new ScenarioOptions(scenarioDirectory, ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioDirectory, scenarioOptions.getPreparerConfigName());
        AVConfigGroup avCg = new AVConfigGroup();
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath(), avCg);
        AVConfig avC = ProvideAVConfig.with(config, avCg);
        AVGeneratorConfig genConfig = avC.getOperatorConfigs().iterator().next().getGeneratorConfig();
        int numRt = (int) genConfig.getNumberOfVehicles();
        int endTime = (int) config.qsim().getEndTime();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();
        Population population = scenario.getPopulation();
        numVNodes = scenarioOptions.getNumVirtualNodes();
        VirtualNetworkCreator virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();

        /* generate nCreations networks */
        for (int i = 0; i < nCreations; ++i) {
            virtualNetworks.add(virtualNetworkCreator.create(network, population, scenarioOptions, numRt, endTime));
        }

    }

    @Test
    public void testCreation() {
        // ensure that every property is the same for every combination of virtual
        // networks
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
}
