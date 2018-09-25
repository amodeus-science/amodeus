/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.traveldata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
import ch.ethz.idsc.amodeus.prep.PopulationTools;
import ch.ethz.idsc.amodeus.prep.Request;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkCreator;
import ch.ethz.idsc.amodeus.test.TestFileHandling;
import ch.ethz.idsc.amodeus.testutils.TestUtils;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.io.ProvideAVConfig;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.framework.AVConfigGroup;

public class PopulationToolsTestVN2 {
    private static VirtualNetwork<Link> virtualNetwork2;
    private static ScenarioOptions scenarioOptions;
    private static Population population;
    private static Network network;
    private static Set<Request> requestsSingle3 = new HashSet<>();
    private static Set<Request> requestsEmpty = Collections.emptySet();
    private static Set<Request> requests3 = new HashSet<>();

    @BeforeClass
    public static void setup() throws IOException {
        // copy scenario data into main directory
        File scenarioDirectory = new File(TestUtils.getSuperFolder("amodeus"), "resources/testScenario");
        File workingDirectory = MultiFileTools.getWorkingDirectory();
        GlobalAssert.that(workingDirectory.exists());
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath());

        /* input data */
        scenarioOptions = new ScenarioOptions(scenarioDirectory, ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioDirectory, scenarioOptions.getPreparerConfigName());
        AVConfigGroup avCg = new AVConfigGroup();
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath(), avCg);
        AVConfig avC = ProvideAVConfig.with(config, avCg);
        AVGeneratorConfig genConfig = avC.getOperatorConfigs().iterator().next().getGeneratorConfig();
        int numRt = (int) genConfig.getNumberOfVehicles();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        network = scenario.getNetwork();
        population = scenario.getPopulation();

        // create 2 node virtual network
        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "2");
        VirtualNetworkCreator virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();
        virtualNetwork2 = virtualNetworkCreator.create(network, population, scenarioOptions, numRt);

        Link node0 = (Link) virtualNetwork2.getVirtualNode(0).getLinks().toArray()[0];
        Link node1 = (Link) virtualNetwork2.getVirtualNode(1).getLinks().toArray()[0];

        requestsSingle3.add(new Request(10, node0, node1));
        requests3.add(new Request(0, node0, node1));
        requests3.add(new Request(3600, node1, node0));
        requests3.add(new Request(24 * 3600 - 1, node1, node0));
        requests3.add(new Request(3600, node0, node0));

    }

    @Test
    public void testInvalid() {
        try {
            PopulationTools.getLambdaInVirtualNodesAndTimeIntervals(requestsSingle3, virtualNetwork2, 3601);
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }

        try {
            PopulationTools.getLambdaInVirtualNodesAndTimeIntervals(requestsSingle3, virtualNetwork2, -1);
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }
    }

    @Test
    public void testEmpty() {
        Tensor lambda = PopulationTools.getLambdaInVirtualNodesAndTimeIntervals(requestsEmpty, virtualNetwork2, 3600);
        assertEquals(lambda, Array.zeros(24, 2, 2));
    }

    @Test
    public void testVirtualNetwork2() {
        Tensor lambda = PopulationTools.getLambdaInVirtualNodesAndTimeIntervals(requestsSingle3, virtualNetwork2, 12 * 3600);
        assertEquals(lambda, Tensors.of(Tensors.of(Tensors.vector(0, 1), Tensors.vector(0, 0)), Tensors.of(Tensors.vector(0, 0), Tensors.vector(0, 0))));

        lambda = PopulationTools.getLambdaInVirtualNodesAndTimeIntervals(requests3, virtualNetwork2, 12 * 3600);
        assertEquals(lambda, Tensors.of(Tensors.of(Tensors.vector(1, 1), Tensors.vector(1, 0)), Tensors.of(Tensors.vector(0, 0), Tensors.vector(1, 0))));
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }
}
