/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

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
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;

public class LPMinFlowTester {
    private static VirtualNetwork<Link> virtualNetwork2;
    private static VirtualNetwork<Link> virtualNetwork3;
    private static VirtualNetwork<Link> virtualNetwork3incomplete; // the virtual links are: 0<->1, 0<->2
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
        virtualNetwork2 = virtualNetworkCreator.create(network, population, scenarioOptions);

        // create 3 node virtual network
        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "3");
        virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();
        virtualNetwork3 = virtualNetworkCreator.create(network, population, scenarioOptions);

        // create 3 node virtual network incomplete
        scenarioOptions.setProperty(ScenarioOptionsBase.COMPLETEGRAPHIDENTIFIER, "false");
        virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();
        virtualNetwork3incomplete = virtualNetworkCreator.create(network, population, scenarioOptions);
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

    @Test
    public void testLPMinFlow2Nodes() {
        // init LPMinFlow
        LPMinFlow lpMinFlow = new LPMinFlow(virtualNetwork2);
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Array.zeros(2, 2));

        // test trivial case
        lpMinFlow.initiateLP();
        lpMinFlow.solveLP(false, Tensors.vector(0, 0));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Array.zeros(2, 2));

        // test infeasible case, sum of minFlow argument has to be less or equal zero
        try {
            lpMinFlow.solveLP(false, Tensors.vector(1, 0));
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }

        // test infeasible case, the input variable has to be almost integer
        try {
            lpMinFlow.solveLP(false, Tensors.vector(-0.1, 0));
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }

        // test simple case without any action
        lpMinFlow.solveLP(false, Tensors.vector(0, -1));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0), Tensors.vector(0, 0)));

        // test simple case without any action and rounded input
        lpMinFlow.solveLP(false, Tensors.vector(0, -0.00001));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0), Tensors.vector(0, 0)));

        // test simple case with one action
        lpMinFlow.solveLP(false, Tensors.vector(1, -1));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0), Tensors.vector(1, 0)));

        // test other simple case with one action
        lpMinFlow.solveLP(false, Tensors.vector(-1, 1));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 1), Tensors.vector(0, 0)));

        // test mixed case
        lpMinFlow.solveLP(false, Tensors.vector(-3, 2));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 2), Tensors.vector(0, 0)));

        lpMinFlow.closeLP();
    }

    @Test
    public void testLPMinFlow3Nodes() {
        // init LPMinFlow
        LPMinFlow lpMinFlow = new LPMinFlow(virtualNetwork3);
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Array.zeros(3, 3));

        // test trivial case
        lpMinFlow.initiateLP();
        lpMinFlow.solveLP(false, Tensors.vector(0, 0, 0));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Array.zeros(3, 3));

        // test infeasible case, sum of minFlow argument has to be less or equal zero
        try {
            lpMinFlow.solveLP(false, Tensors.vector(1, 0, 0));
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }

        // test infeasible case, the input variable has to be almost integer
        try {
            lpMinFlow.solveLP(false, Tensors.vector(-0.1, 0, 0));
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }

        // test simple case without any action
        lpMinFlow.solveLP(false, Tensors.vector(0, -1, 0));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0)));

        // test simple case without any action and rounded input
        lpMinFlow.solveLP(false, Tensors.vector(0, -0.00001, 0));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0)));

        // test simple case with one action
        lpMinFlow.solveLP(false, Tensors.vector(1, -1, 0));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 0), Tensors.vector(1, 0, 0), Tensors.vector(0, 0, 0)));

        // test other simple case with one action
        lpMinFlow.solveLP(false, Tensors.vector(0, -1, 1));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 1), Tensors.vector(0, 0, 0)));

        // test other simple case with one action
        lpMinFlow.solveLP(false, Tensors.vector(-1, 0, 1));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 1), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0)));

        // test case with two actions
        lpMinFlow.solveLP(false, Tensors.vector(-1, 2, -1));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 1, 0), Tensors.vector(0, 0, 0), Tensors.vector(0, 1, 0)));

        // test case with two actions
        lpMinFlow.solveLP(false, Tensors.vector(-2, 1, 1));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 1, 1), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0)));

        // test mixed case
        lpMinFlow.solveLP(false, Tensors.vector(-3, -3, 3)); // node 0 is closer to node 2
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 3), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0)));

        // test mixed case
        lpMinFlow.solveLP(false, Tensors.vector(-3, -3, 4)); // node 0 is closer to node 2
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 3), Tensors.vector(0, 0, 1), Tensors.vector(0, 0, 0)));

        // test mixed case
        lpMinFlow.solveLP(false, Tensors.vector(-1, -2, -3));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0)));

        lpMinFlow.closeLP();
    }

    @Test
    public void testLPMinFlow3NodesIncomplete() {
        // init LPMinFlow
        LPMinFlow lpMinFlow = new LPMinFlow(virtualNetwork3incomplete); // the virtual links are: 0<->1, 0<->2
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Array.zeros(3, 3));

        // test trivial case
        lpMinFlow.initiateLP();
        lpMinFlow.solveLP(false, Tensors.vector(0, 0, 0));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Array.zeros(3, 3));

        // test infeasible case, sum of minFlow argument has to be less or equal zero
        try {
            lpMinFlow.solveLP(false, Tensors.vector(1, 0, 0));
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }

        // test infeasible case, the input variable has to be almost integer
        try {
            lpMinFlow.solveLP(false, Tensors.vector(-0.1, 0, 0));
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }

        // test simple case without any action
        lpMinFlow.solveLP(false, Tensors.vector(0, -1, 0));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0)));

        // test simple case without any action and rounded input
        lpMinFlow.solveLP(false, Tensors.vector(0, -0.00001, 0));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0)));

        // test simple case with one action
        lpMinFlow.solveLP(false, Tensors.vector(1, -1, 0));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 0), Tensors.vector(1, 0, 0), Tensors.vector(0, 0, 0)));

        // test other simple case with one action
        lpMinFlow.solveLP(false, Tensors.vector(-1, 0, 1));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 1), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0)));

        // test other simple case with one action
        lpMinFlow.solveLP(false, Tensors.vector(0, -1, 1)); // needs to be rebalanced over node 0
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 1), Tensors.vector(1, 0, 0), Tensors.vector(0, 0, 0)));

        // test case with two actions
        lpMinFlow.solveLP(false, Tensors.vector(2, -1, -1));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 0), Tensors.vector(1, 0, 0), Tensors.vector(1, 0, 0)));

        // test case with two actions
        lpMinFlow.solveLP(false, Tensors.vector(-2, 1, 1));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 1, 1), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0)));

        // test case with two actions
        lpMinFlow.solveLP(false, Tensors.vector(1, -2, 1)); // needs to be rebalanced over node 0
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 1), Tensors.vector(2, 0, 0), Tensors.vector(0, 0, 0)));

        // test mixed case
        lpMinFlow.solveLP(false, Tensors.vector(-3, -3, 3)); // node 0 is closer to node 2
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 3), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0)));

        // test mixed case
        lpMinFlow.solveLP(false, Tensors.vector(-3, -3, 4)); // needs to be rebalanced over node 0
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 4), Tensors.vector(1, 0, 0), Tensors.vector(0, 0, 0)));

        // test mixed case
        lpMinFlow.solveLP(false, Tensors.vector(-1, -2, -3));
        assertEquals(lpMinFlow.getAlphaAbsolute_ij(), Tensors.of(Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0)));

        lpMinFlow.closeLP();
    }
}
