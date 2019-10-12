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
import ch.ethz.idsc.amodeus.prep.PopulationArrivalRate;
import ch.ethz.idsc.amodeus.prep.Request;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkCreator;
import ch.ethz.idsc.amodeus.test.TestFileHandling;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.GeneratorConfig;

public class PopulationToolsTestVN3 {
    private static VirtualNetwork<Link> virtualNetwork3;
    private static ScenarioOptions scenarioOptions;
    private static Population population;
    private static Network network;
    private static Set<Request> requestsSingle3 = new HashSet<>();
    private static Set<Request> requestsEmpty = Collections.emptySet();
    private static Set<Request> requests3 = new HashSet<>();
    private static int endTime;

    @BeforeClass
    public static void setUp() throws IOException {
        // copy scenario data into main directory
        File scenarioDirectory = new File(Locate.repoFolder(PopulationToolsTestVN3.class, "amodeus"), "resources/testScenario");
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        GlobalAssert.that(workingDirectory.exists());
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath());

        /* input data */
        scenarioDirectory = new File(Locate.repoFolder(PopulationToolsTestVN3.class, "amodeus"), "resources/testScenario");
        scenarioOptions = new ScenarioOptions(scenarioDirectory, ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        AVConfigGroup avCg = new AVConfigGroup();
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath(), avCg);
        GeneratorConfig genConfig = avCg.getOperatorConfigs().values().iterator().next().getGeneratorConfig();
        int numRt = genConfig.getNumberOfVehicles();
        endTime = (int) config.qsim().getEndTime();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        network = scenario.getNetwork();
        population = scenario.getPopulation();

        // create 3 node virtual network
        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "3");
        VirtualNetworkCreator virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();
        virtualNetwork3 = virtualNetworkCreator.create(network, population, scenarioOptions, numRt, endTime);

        Link node0 = (Link) virtualNetwork3.getVirtualNode(0).getLinks().toArray()[0]; // in both virtual networks in virtual node 0
        Link node1 = (Link) virtualNetwork3.getVirtualNode(1).getLinks().toArray()[0]; // in virtualNetwork2 in virtual node 0
        Link node2 = (Link) virtualNetwork3.getVirtualNode(2).getLinks().toArray()[0]; // in virtualNetwork2 in virtual node 1

        requestsSingle3.add(new Request(10, node0, node2));
        requests3.add(new Request(0, node0, node2));
        requests3.add(new Request(3600, node1, node2));
        requests3.add(new Request(30 * 3600 - 1, node1, node0));
        requests3.add(new Request(3600, node2, node2));
    }

    @Test
    public void testEmpty() {
        Tensor lambda = PopulationArrivalRate.getVNodeAndInterval(requestsEmpty, virtualNetwork3, 3600, endTime);
        assertEquals(lambda, Array.zeros(30, 3, 3));
    }

    @Test
    public void testVirtualNetwork3() {
        Tensor lambda = PopulationArrivalRate.getVNodeAndInterval(requestsSingle3, virtualNetwork3, 15 * 3600, endTime);
        assertEquals(lambda, Tensors.of(Tensors.of(Tensors.vector(0, 0, 1), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0)),
                Tensors.of(Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0))));

        lambda = PopulationArrivalRate.getVNodeAndInterval(requests3, virtualNetwork3, 15 * 3600, endTime);
        assertEquals(lambda, Tensors.of(Tensors.of(Tensors.vector(0, 0, 1), Tensors.vector(0, 0, 1), Tensors.vector(0, 0, 1)),
                Tensors.of(Tensors.vector(0, 0, 0), Tensors.vector(1, 0, 0), Tensors.vector(0, 0, 0))));
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }
}
