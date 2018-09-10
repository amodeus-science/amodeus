/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.traveldata;

import static org.junit.Assert.assertEquals;

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
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;

public class PopulationToolsTestVN3 {
    private static VirtualNetwork<Link> virtualNetwork3;
    private static ScenarioOptions scenarioOptions;
    private static Population population;
    private static Network network;
    private static Set<Request> requestsSingle3 = new HashSet<>();
    private static Set<Request> requestsEmpty = Collections.emptySet();
    private static Set<Request> requests3 = new HashSet<>();

    @BeforeClass
    public static void setUp() throws IOException {
        // copy scenario data into main directory
        File scenarioDirectory = new File(TestUtils.getSuperFolder("amodeus"), "resources/testScenario");
        File workingDirectory = MultiFileTools.getWorkingDirectory();
        GlobalAssert.that(workingDirectory.exists());
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath());

        /* input data */
        scenarioDirectory = new File(TestUtils.getSuperFolder("amodeus"), "resources/testScenario");
        scenarioOptions = new ScenarioOptions(scenarioDirectory, ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioDirectory, scenarioOptions.getPreparerConfigName());
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath());
        Scenario scenario = ScenarioUtils.loadScenario(config);
        network = scenario.getNetwork();
        population = scenario.getPopulation();

        // create 3 node virtual network
        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "3");
        VirtualNetworkCreator virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();
        virtualNetwork3 = virtualNetworkCreator.create(network, population, scenarioOptions);

        Link node0 = (Link) virtualNetwork3.getVirtualNode(0).getLinks().toArray()[0]; // in both virtual networks in virtual node 0
        Link node1 = (Link) virtualNetwork3.getVirtualNode(1).getLinks().toArray()[0]; // in virtualNetwork2 in virtual node 0
        Link node2 = (Link) virtualNetwork3.getVirtualNode(2).getLinks().toArray()[0]; // in virtualNetwork2 in virtual node 1

        requestsSingle3.add(new Request(10, node0, node2));
        requests3.add(new Request(0, node0, node2));
        requests3.add(new Request(3600, node1, node2));
        requests3.add(new Request(24 * 3600 - 1, node1, node0));
        requests3.add(new Request(3600, node2, node2));
    }

    @Test
    public void testEmpty() {
        Tensor lambda = PopulationTools.getLambdaInVirtualNodesAndTimeIntervals(requestsEmpty, virtualNetwork3, 900);
        assertEquals(lambda, Array.zeros(96, 3, 3));
    }

    @Test
    public void testVirtualNetwork3() {
        Tensor lambda = PopulationTools.getLambdaInVirtualNodesAndTimeIntervals(requestsSingle3, virtualNetwork3, 12 * 3600);
        assertEquals(lambda, Tensors.of(Tensors.of(Tensors.vector(0, 0, 1), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0)),
                Tensors.of(Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0))));

        lambda = PopulationTools.getLambdaInVirtualNodesAndTimeIntervals(requests3, virtualNetwork3, 12 * 3600);
        assertEquals(lambda, Tensors.of(Tensors.of(Tensors.vector(0, 0, 1), Tensors.vector(0, 0, 1), Tensors.vector(0, 0, 1)),
                Tensors.of(Tensors.vector(0, 0, 0), Tensors.vector(1, 0, 0), Tensors.vector(0, 0, 0))));
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }
}
