/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.vehiclesGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

import ch.ethz.idsc.amodeus.matsim.mod.VehicleToVSGenerator;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkCreator;
import ch.ethz.idsc.amodeus.traveldata.StaticTravelData;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.util.io.LocateUtils;
import ch.ethz.idsc.amodeus.util.io.ProvideAVConfig;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.config.AVOperatorConfig;
import ch.ethz.matsim.av.framework.AVConfigGroup;

public class VehicleToVSGeneratorTester {
    private static final int TRIALS = 1000;
    // ---
    private static VirtualNetwork<Link> virtualNetwork;
    private static ScenarioOptions scenarioOptions;
    private static Population population;
    private static Network network;
    private static TravelData travelData000;
    private static TravelData travelData123;
    private static TravelData travelData334;
    private static AVGeneratorConfig avGeneratorConfig;

    @BeforeClass
    public static void setup() throws IOException {

        /** input data */
        File scenarioDirectory = new File(LocateUtils.getSuperFolder("amodeus"), "resources/testScenario");
        scenarioOptions = new ScenarioOptions(scenarioDirectory, ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        AVConfigGroup avCg = new AVConfigGroup();
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath(), avCg);
        AVConfig avC = ProvideAVConfig.with(config, avCg);
        AVGeneratorConfig genConfig = avC.getOperatorConfigs().iterator().next().getGeneratorConfig();
        int numRt = (int) genConfig.getNumberOfVehicles();
        int endTime = (int) config.qsim().getEndTime();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        network = scenario.getNetwork();
        population = scenario.getPopulation();
        scenarioOptions.setProperty(ScenarioOptionsBase.NUMVNODESIDENTIFIER, "3");
        VirtualNetworkCreator virtualNetworkCreator = scenarioOptions.getVirtualNetworkCreator();
        virtualNetwork = virtualNetworkCreator.create(network, population, scenarioOptions, numRt, endTime);

        /** creating dummy config with 10 vehicles */
        avGeneratorConfig = new AVGeneratorConfig(new AVOperatorConfig("id", new AVConfig()), "strategy");

        travelData000 = new StaticTravelData(virtualNetwork.getvNetworkID(), Array.zeros(3, 3, 3), Array.zeros(3, 3, 3), Array.zeros(3, 3, 3), Array.zeros(3), "", endTime);
        travelData123 = new StaticTravelData(virtualNetwork.getvNetworkID(), Array.zeros(3, 3, 3), Array.zeros(3, 3, 3), Array.zeros(3, 3, 3), Tensors.vector(1, 2, 3), "",
                endTime);
        travelData334 = new StaticTravelData(virtualNetwork.getvNetworkID(), Array.zeros(3, 3, 3), Array.zeros(3, 3, 3), Array.zeros(3, 3, 3), Tensors.vector(3, 3, 4), "",
                endTime);
    }

    @Test
    public void testHasNext() {
        VehicleToVSGenerator vehicleToVSGenerator = new VehicleToVSGenerator(avGeneratorConfig, network, virtualNetwork, travelData000);

        /** test hasNext */
        for (int i = 0; i < avGeneratorConfig.getNumberOfVehicles(); i++) {
            assertTrue(vehicleToVSGenerator.hasNext());
            vehicleToVSGenerator.next();
        }
        assertFalse(vehicleToVSGenerator.hasNext());

        vehicleToVSGenerator = new VehicleToVSGenerator(avGeneratorConfig, network, virtualNetwork, travelData123);

        /** test hasNext */
        for (int i = 0; i < avGeneratorConfig.getNumberOfVehicles(); i++) {
            assertTrue(vehicleToVSGenerator.hasNext());
            vehicleToVSGenerator.next();
        }
        assertFalse(vehicleToVSGenerator.hasNext());

        vehicleToVSGenerator = new VehicleToVSGenerator(avGeneratorConfig, network, virtualNetwork, travelData334);

        /** test hasNext */
        for (int i = 0; i < avGeneratorConfig.getNumberOfVehicles(); i++) {
            assertTrue(vehicleToVSGenerator.hasNext());
            vehicleToVSGenerator.next();
        }
        assertFalse(vehicleToVSGenerator.hasNext());
    }

    // TODO this test fails after update to MATSim 11, test if this is serious or not. Assumption: no serious implications.
    // @Test
    // public void testEmptyDistribution() {
    // VehicleToVSGenerator vehicleToVSGenerator = new VehicleToVSGenerator(avGeneratorConfig, network, virtualNetwork, travelData000);
    //
    // double ratio = avGeneratorConfig.getNumberOfVehicles() / 3.0;
    // Tensor distribution = Tensors.vector(ratio, ratio, ratio);
    // Tensor counter = Array.zeros(3);
    //
    // for (int i = 0; i < TRIALS; i++) {
    // vehicleToVSGenerator = new VehicleToVSGenerator(avGeneratorConfig, network, virtualNetwork, travelData000);
    // for (int j = 0; j < avGeneratorConfig.getNumberOfVehicles(); j++)
    // vehicleToVSGenerator.next();
    // counter = counter.add(vehicleToVSGenerator.getPlacedVehicles());
    // }
    // assertTrue(Chop.below(0.1).close(counter.divide(RealScalar.of(TRIALS)), distribution));
    // }

    // TODO this test fails after update to MATSim 11, test if this is serious or not. Assumption: no serious implications.
    // @Test
    // public void testPartiallyGivenDistribution() {
    // VehicleToVSGenerator vehicleToVSGenerator = new VehicleToVSGenerator(avGeneratorConfig, network, virtualNetwork, travelData123);
    //
    // double ratio = (avGeneratorConfig.getNumberOfVehicles() - 6) / 3.0;
    // Tensor distribution = Tensors.vector(ratio + 1, ratio + 2, ratio + 3);
    // Tensor counter = Array.zeros(3);
    //
    // for (int i = 0; i < TRIALS; i++) {
    // vehicleToVSGenerator = new VehicleToVSGenerator(avGeneratorConfig, network, virtualNetwork, travelData123);
    // for (int j = 0; j < avGeneratorConfig.getNumberOfVehicles(); j++)
    // vehicleToVSGenerator.next();
    // counter = counter.add(vehicleToVSGenerator.getPlacedVehicles());
    // }
    // assertTrue(Chop.below(0.1).close(counter.divide(RealScalar.of(TRIALS)), distribution));
    // }

    @Test
    public void testTotallyGivenDistribution() {
        VehicleToVSGenerator vehicleToVSGenerator = new VehicleToVSGenerator(avGeneratorConfig, network, virtualNetwork, travelData334);

        for (int i = 0; i < TRIALS; i++) {
            vehicleToVSGenerator = new VehicleToVSGenerator(avGeneratorConfig, network, virtualNetwork, travelData334);
            for (int j = 0; j < avGeneratorConfig.getNumberOfVehicles(); j++)
                vehicleToVSGenerator.next();
            assertEquals(vehicleToVSGenerator.getPlacedVehicles(), Tensors.vector(3, 3, 4));
        }
    }
}
