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

import ch.ethz.idsc.amodeus.analysis.ScenarioParametersExport;
import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.testutils.SharedTestServer;
import ch.ethz.idsc.amodeus.testutils.TestPreparer;
import ch.ethz.idsc.amodeus.testutils.TestUtils;
import ch.ethz.idsc.amodeus.traveldata.TravelDataTestHelper;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetworkGet;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetworkIO;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Total;

public class SharedRoboTaxiTest {

    private static TestPreparer testPreparer;
    private static SharedTestServer testServer;
    private static VirtualNetwork<Link> vNCreated;
    private static VirtualNetwork<Link> vNSaved;

    @BeforeClass
    public static void setUpOnce() throws Exception {
        System.out.print("GLPK version is: ");
        System.out.println(GLPK.glp_version());

        // copy scenario data into main directory
        File scenarioDirectory = new File(TestUtils.getSuperFolder("amodeus"), "resources/testScenario");
        File workingDirectory = MultiFileTools.getWorkingDirectory();
        GlobalAssert.that(workingDirectory.exists());
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath());

        // run scenario preparer
        testPreparer = TestPreparer.run().on(workingDirectory);

        // run scenario server
        testServer = SharedTestServer.run().on(workingDirectory);

        // prepare travel data test
        vNCreated = VirtualNetworkGet.readDefault(testPreparer.getPreparedNetwork());
        Map<String, Link> map = new HashMap<>();
        testPreparer.getPreparedNetwork().getLinks().entrySet().forEach(e -> map.put(e.getKey().toString(), e.getValue()));
        vNSaved = VirtualNetworkIO.fromByte(map, new File("resources/testComparisonFiles/virtualNetwork"));
    }

    @Test
    public void testPreparer() throws Exception {
        System.out.print("GLPK version is: ");
        System.out.println(GLPK.glp_version());
        System.out.print("Preparer Test:\t");

        /** setup of scenario */
        File preparedPopulationFile = new File("preparedPopulation.xml");
        GlobalAssert.that(preparedPopulationFile.exists());

        File preparedNetworkFile = new File("preparedNetwork.xml");
        GlobalAssert.that(preparedNetworkFile.exists());

        File config = new File("config.xml");
        GlobalAssert.that(config.exists());

        Network originalNetwork = NetworkLoader.fromConfigFile(testServer.getConfigFile());
        Network preparedNetwork = testPreparer.getPreparedNetwork();
        GlobalAssert.that(Objects.nonNull(originalNetwork));
        GlobalAssert.that(Objects.nonNull(preparedNetwork));
    }

    @Test
    public void testServer() throws Exception {
        System.out.print("GLPK version is: ");
        System.out.println(GLPK.glp_version());

        System.out.print("Server Test:\t");

        /** scenario options */
        File workingDirectory = MultiFileTools.getWorkingDirectory();
        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        assertEquals("config.xml", scenarioOptions.getSimulationConfigName());
        assertEquals("preparedNetwork", scenarioOptions.getPreparedNetworkName());
        assertEquals("preparedPopulation", scenarioOptions.getPreparedPopulationName());

        /** simulation objects should exist after simulation (simulation data) */
        File simobj = new File("output/001/simobj/it.00");
        assertTrue(simobj.exists());
        assertEquals(109, simobj.listFiles().length);
        assertTrue(new File(simobj, "0108000/0108000.bin").exists());
        assertTrue(new File(simobj, "0000000/0000010.bin").exists());

    }

    // TODO add more tests for shared functionality
    @Test
    public void testAnalysis() throws Exception {
        System.out.print("Analysis Test:\t");

        // TODO add tests for shared

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

        assertEquals(0.2048175925925926, occupancyRatio.number().doubleValue(), 0.0);

        assertEquals(0.8072881014416966, distanceRatio.number().doubleValue(), 0.0);

        /** fleet distances */
        assertTrue(ate.getDistancElement().totalDistance >= 0.0);
        assertEquals(91404.89496291742, ate.getDistancElement().totalDistance, 0.0);
        assertTrue(ate.getDistancElement().totalDistanceWtCst >= 0.0);
        assertEquals(83248.82545402774, ate.getDistancElement().totalDistanceWtCst, 0.0);
        assertTrue(ate.getDistancElement().totalDistancePicku > 0.0);
        assertEquals(8156.069508889132, ate.getDistancElement().totalDistancePicku, 0.0);
        assertTrue(ate.getDistancElement().totalDistanceRebal >= 0.0);
        assertEquals(0.0, ate.getDistancElement().totalDistanceRebal, 0.0);
        assertTrue(ate.getDistancElement().totalDistanceRatio >= 0.0);
        assertEquals(0.910769882595472, ate.getDistancElement().totalDistanceRatio, 0.0);
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
        assertTrue((new File("output/001/data/binnedWaitingTimes.png")).exists());
        assertTrue((new File("output/001/data/distanceDistribution.png")).exists());
        assertTrue((new File("output/001/data/occAndDistRatios.png")).exists());
        assertTrue((new File("output/001/data/stackedDistance.png")).exists());
        assertTrue((new File("output/001/data/statusDistribution.png")).exists());
        assertTrue((new File("output/001/data", ScenarioParametersExport.FILENAME)).exists());
        assertTrue((new File("output/001/data/WaitingTimes")).isDirectory());
        assertTrue((new File("output/001/data/WaitingTimes/WaitingTimes.mathematica")).exists());
        assertTrue((new File("output/001/data/StatusDistribution")).isDirectory());
        assertTrue((new File("output/001/data/StatusDistribution/StatusDistribution.mathematica")).exists());
        assertTrue((new File("output/001/data/DistancesOverDay")).isDirectory());
        assertTrue((new File("output/001/data/DistancesOverDay/DistancesOverDay.mathematica")).exists());
        assertTrue((new File("output/001/data/DistanceRatios")).isDirectory());
        assertTrue((new File("output/001/data/DistanceRatios/DistanceRatios.mathematica")).exists());
        assertTrue((new File("output/001/report/report.html")).exists());
        assertTrue((new File("output/001/report/av.xml")).exists());
        assertTrue((new File("output/001/report/config.xml")).exists());
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }
}
