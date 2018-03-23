/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.gnu.glpk.GLPK;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.testutils.TestPreparer;
import ch.ethz.idsc.amodeus.testutils.TestServer;
import ch.ethz.idsc.amodeus.testutils.TestUtils;
import ch.ethz.idsc.amodeus.traveldata.TravelDataTestHelper;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetworkGet;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetworkIO;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Total;

public class ScenarioPipeLineTest {

    private static final File WORKING_DIRECTORY = new File("__test_working_directory");
    private static File activeWorkingDirectory = null;

    private static TestPreparer testPreparer;
    private static TestServer testServer;
    private static VirtualNetwork<Link> vNCreated;
    private static VirtualNetwork<Link> vNSaved;
    private static TravelDataTestHelper travelDataTestHelper;

    @BeforeClass
    public static void setUpOnce() throws Exception {
        if (WORKING_DIRECTORY.exists()) {
            throw new IllegalStateException("To run the test the working directory must be deleted: " + WORKING_DIRECTORY.getAbsolutePath());
        }

        if (!WORKING_DIRECTORY.mkdir()) {
            throw new IllegalStateException("Cannot create test working directory: " + WORKING_DIRECTORY.getAbsolutePath());
        }

        activeWorkingDirectory = WORKING_DIRECTORY;

        System.out.print("GLPK version is: ");
        System.out.println(GLPK.glp_version());

        // copy scenario data into main directory
        File scenarioDirectory = new File(TestUtils.getSuperFolder("amodeus"), "resources/testScenario");
        File workingDirectory = activeWorkingDirectory;
        GlobalAssert.that(workingDirectory.exists());
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath());

        // run scenario preparer
        testPreparer = TestPreparer.run().on(workingDirectory);

        // run scenario server
        testServer = TestServer.run().on(workingDirectory);

        // prepare travel data test
        vNCreated = VirtualNetworkGet.readDefault(testPreparer.getPreparedNetwork(), workingDirectory);
        Map<String, Link> map = new HashMap<>();
        testPreparer.getPreparedNetwork().getLinks().entrySet().forEach(e -> map.put(e.getKey().toString(), e.getValue()));
        vNSaved = VirtualNetworkIO.fromByte(map, new File("resources/testComparisonFiles/virtualNetwork"));
        travelDataTestHelper = TravelDataTestHelper.prepare(vNCreated, vNSaved, workingDirectory);

    }

    @Test
    public void testPreparer() throws Exception {
        System.out.print("GLPK version is: ");
        System.out.println(GLPK.glp_version());

        System.out.print("Preparer Test:\t");

        // creation of files
        File preparedPopulationFile = new File(WORKING_DIRECTORY, "preparedPopulation.xml");
        assertTrue(preparedPopulationFile.exists());

        File preparedNetworkFile = new File(WORKING_DIRECTORY, "preparedNetwork.xml");
        assertTrue(preparedNetworkFile.exists());

        File config = new File(WORKING_DIRECTORY, "config.xml");
        assertTrue(config.exists());

        // consistency of network (here no cutting)
        Network originalNetwork = NetworkLoader.loadNetwork(testServer.getConfigFile());
        Network preparedNetwork = testPreparer.getPreparedNetwork();
        GlobalAssert.that(Objects.nonNull(originalNetwork));
        GlobalAssert.that(Objects.nonNull(preparedNetwork));
        assertEquals(preparedNetwork.getNodes().size(), originalNetwork.getNodes().size());
        assertEquals(preparedNetwork.getLinks().size(), originalNetwork.getLinks().size());

        // consistency of population
        assertEquals(2000, testPreparer.getPreparedPopulation().getPersons().size());

        // consistency of virtualNetwork
        assertEquals(testServer.getScenarioOptions().getNumVirtualNodes(), vNCreated.getVirtualNodes().size());
        assertEquals(vNSaved.getVirtualNodes().size(), vNCreated.getVirtualNodes().size());
        assertEquals(vNSaved.getVirtualLinks().size(), vNCreated.getVirtualLinks().size());
        assertEquals(vNSaved.getVirtualLink(0).getId(), vNCreated.getVirtualLink(0).getId());
        assertEquals(vNSaved.getVirtualLink(5).getFrom().getId(), vNCreated.getVirtualLink(5).getFrom().getId());
        assertEquals(vNSaved.getVirtualLink(6).getTo().getId(), vNCreated.getVirtualLink(6).getTo().getId());
        assertEquals(vNSaved.getVirtualNode(0).getLinks().size(), vNCreated.getVirtualNode(0).getLinks().size());
        assertEquals(vNSaved.getVirtualNode(1).getLinks().size(), vNCreated.getVirtualNode(1).getLinks().size());
        assertEquals(vNSaved.getVirtualNode(2).getLinks().size(), vNCreated.getVirtualNode(2).getLinks().size());
        assertEquals(vNSaved.getVirtualNode(3).getLinks().size(), vNCreated.getVirtualNode(3).getLinks().size());

        // consistency of travelData
        assertTrue(travelDataTestHelper.tDCheck());
        assertTrue(travelDataTestHelper.timeStepsCheck());

        assertTrue(travelDataTestHelper.lambdaCheck());
        assertTrue(travelDataTestHelper.lambdaijPSFCheck());
        assertTrue(travelDataTestHelper.pijCheck());
        assertTrue(travelDataTestHelper.pijPSFCheck());
        assertTrue(travelDataTestHelper.alphaPSFCheck());

    }

    @Test
    public void testServer() throws Exception {
        System.out.print("GLPK version is: ");
        System.out.println(GLPK.glp_version());

        System.out.print("Server Test:\t");

        // scenario options
        File workingDirectory = activeWorkingDirectory;
        ScenarioOptions scenarioOptions = ScenarioOptions.load(workingDirectory);
        assertEquals("config.xml", scenarioOptions.getSimulationConfigName());
        assertEquals("preparedNetwork", scenarioOptions.getPreparedNetworkName());
        assertEquals("preparedPopulation", scenarioOptions.getPreparedPopulationName());

        // simulation objects should exist after simulation (simulation data)
        File simobj = new File(WORKING_DIRECTORY, "output/simobj/it.00");
        assertTrue(simobj.exists());
        assertEquals(109, simobj.listFiles().length);
        assertTrue(new File(simobj, "0108000/0108000.bin").exists());
        assertTrue(new File(simobj, "0000000/0000010.bin").exists());

    }

    @Test
    public void testAnalysis() throws Exception {
        System.out.print("Analysis Test:\t");

        AnalysisTestExport ate = testServer.getAnalysisTestExport();

        /** number of processed requests */
        assertEquals(2000, ate.getSimulationInformationElement().reqsize());

        /** fleet size */
        assertEquals(200, ate.getSimulationInformationElement().vehicleSize());

        /** status distribution, every row must equal the total of vehicles */
        Tensor distributionSum = Total.of(Transpose.of(ate.getStatusDistribution().statusTensor));
        distributionSum.flatten(-1).forEach(e -> //
        assertTrue(e.equals(RealScalar.of(ate.getSimulationInformationElement().vehicleSize()))));

        /** distance and occupancy ratios */
        Scalar occupancyRatio = Mean.of(ate.getDistancElement().ratios).Get(0);
        Scalar distanceRatio = Mean.of(ate.getDistancElement().ratios).Get(1);
        assertTrue(occupancyRatio.equals(RationalScalar.of(35729, 432000)));
        assertTrue(distanceRatio.equals(RealScalar.of(0.6757250816100977)));

        /** fleet distances */
        assertTrue(ate.getDistancElement().totalDistance >= 0.0);
        assertEquals(34754.7000511536, ate.getDistancElement().totalDistance, 0.0);
        assertTrue(ate.getDistancElement().totalDistanceWtCst >= 0.0);
        assertEquals(28974.040196898222, ate.getDistancElement().totalDistanceWtCst, 0.0);
        assertTrue(ate.getDistancElement().totalDistancePicku > 0.0);
        assertEquals(5780.659854255442, ate.getDistancElement().totalDistancePicku, 0.0);
        assertTrue(ate.getDistancElement().totalDistanceRebal >= 0.0);
        assertEquals(0.0, ate.getDistancElement().totalDistanceRebal, 0.0);
        assertTrue(ate.getDistancElement().totalDistanceRatio >= 0.0);
        assertEquals(0.8336725724651016, ate.getDistancElement().totalDistanceRatio, 0.0);
        ate.getDistancElement().totalDistancesPerVehicle.flatten(-1).forEach(s -> //
        assertTrue(Scalars.lessEquals(RealScalar.ZERO, (Scalar) s)));
        assertTrue(((Scalar) Total.of(ate.getDistancElement().totalDistancesPerVehicle)).number().doubleValue() //
        == ate.getDistancElement().totalDistance);
        assertTrue(((Scalar) Total.of(ate.getDistancElement().totalDistancesPerVehicle)).number().doubleValue() //
        == ate.getDistancElement().totalDistance);

        /** waiting Times */
        assertTrue(ate.getWaitingTimes().maximumWaitTime >= 0.0);
        ate.getWaitingTimes().requestWaitTimes.values().stream().forEach(d -> //
        {
            assertTrue(d >= 0.0);//
            assertTrue(d <= ate.getWaitingTimes().maximumWaitTime);
        });
        assertTrue(Scalars.lessEquals(RealScalar.ZERO, ate.getWaitingTimes().totalWaitTimeQuantile.Get(0)));
        assertTrue(Scalars.lessEquals(ate.getWaitingTimes().totalWaitTimeQuantile.Get(0), ate.getWaitingTimes().totalWaitTimeQuantile.Get(1)));
        assertTrue(Scalars.lessEquals(ate.getWaitingTimes().totalWaitTimeQuantile.Get(1), ate.getWaitingTimes().totalWaitTimeQuantile.Get(2)));
        assertTrue(Scalars.lessEquals(ate.getWaitingTimes().totalWaitTimeMean, ate.getWaitingTimes().totalWaitTimeQuantile.Get(2)));
        assertTrue(Scalars.lessEquals(RealScalar.ZERO, ate.getWaitingTimes().totalWaitTimeMean));

        /** presence of plot files */
        assertTrue((new File(WORKING_DIRECTORY, "output/data/binnedWaitingTimes.png")).exists());
        assertTrue((new File(WORKING_DIRECTORY, "output/data/distanceDistribution.png")).exists());
        assertTrue((new File(WORKING_DIRECTORY, "output/data/occAndDistRatios.png")).exists());
        assertTrue((new File(WORKING_DIRECTORY, "output/data/stackedDistance.png")).exists());
        assertTrue((new File(WORKING_DIRECTORY, "output/data/statusDistribution.png")).exists());

        assertTrue((new File(WORKING_DIRECTORY, "output/data/scenarioParameters.obj")).exists());

        assertTrue((new File(WORKING_DIRECTORY, "output/data/WaitingTimes")).isDirectory());
        assertTrue((new File(WORKING_DIRECTORY, "output/data/WaitingTimes/WaitingTimes.mathematica")).exists());

        assertTrue((new File(WORKING_DIRECTORY, "output/data/StatusDistribution")).isDirectory());
        assertTrue((new File(WORKING_DIRECTORY, "output/data/StatusDistribution/StatusDistribution.mathematica")).exists());

        assertTrue((new File(WORKING_DIRECTORY, "output/data/DistancesOverDay")).isDirectory());
        assertTrue((new File(WORKING_DIRECTORY, "output/data/DistancesOverDay/DistancesOverDay.mathematica")).exists());

        assertTrue((new File(WORKING_DIRECTORY, "output/data/DistanceRatios")).isDirectory());
        assertTrue((new File(WORKING_DIRECTORY, "output/data/DistanceRatios/DistanceRatios.mathematica")).exists());

        assertTrue(new File(WORKING_DIRECTORY, "output/report/report.html").exists());
        assertTrue(new File(WORKING_DIRECTORY, "output/report/av.xml").exists());
        assertTrue(new File(WORKING_DIRECTORY, "output/report/config.xml").exists());
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles(activeWorkingDirectory);

        if (activeWorkingDirectory != null) {
            activeWorkingDirectory.delete();
        }
    }
}
