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

import ch.ethz.idsc.amodeus.analysis.AnalysisConstants;
import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.testutils.SharedTestServer;
import ch.ethz.idsc.amodeus.testutils.TestPreparer;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Total;
import ch.ethz.idsc.tensor.sca.Round;

public class SharedRoboTaxiTest {

    private static TestPreparer testPreparer;
    private static SharedTestServer testServer;

    @BeforeClass
    public static void setUpOnce() throws Exception {
        System.out.print("GLPK version is: ");
        System.out.println(GLPK.glp_version());

        // copy scenario data into main directory
        File scenarioDirectory = new File(Locate.repoFolder(SharedRoboTaxiTest.class, "amodeus"), "resources/testScenario");
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        GlobalAssert.that(workingDirectory.isDirectory());
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath());

        // run scenario preparer
        testPreparer = TestPreparer.run(workingDirectory);

        // run scenario server
        testServer = SharedTestServer.run(workingDirectory);

        // prepare travel data test
        // TODO the call VirtualNetworkGet.readDefault below should not be necessary
        // ... or why is it necessary?
        // VirtualNetworkGet.readDefault(testPreparer.getPreparedNetwork(), new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault()));
        Map<String, Link> map = new HashMap<>();
        testPreparer.getPreparedNetwork().getLinks().entrySet().forEach(e -> map.put(e.getKey().toString(), e.getValue()));
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
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        assertEquals(workingDirectory.getAbsolutePath() + "/config.xml", scenarioOptions.getSimulationConfigName());
        assertEquals(workingDirectory.getAbsolutePath() + "/preparedNetwork", scenarioOptions.getPreparedNetworkName());
        assertEquals(workingDirectory.getAbsolutePath() + "/preparedPopulation", scenarioOptions.getPreparedPopulationName());

        /** simulation objects should exist after simulation (simulation data) */
        File simobj = new File("output/001/simobj/it.00");
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

        ScalarAssert scalarAssert = new ScalarAssert();
        scalarAssert.add(RationalScalar.of(16597, 80000), occupancyRatio);
        scalarAssert.add((Scalar) RealScalar.of(0.323966658).map(Round._9), (Scalar) distanceRatio.map(Round._9));

        /** fleet distances */
        assertTrue(Scalars.lessEquals(RealScalar.ZERO, ate.getDistancElement().totalDistance));
        scalarAssert.add((Scalar) RealScalar.of(339062.65383).map(Round._5), (Scalar) ate.getDistancElement().totalDistance.map(Round._5));
        assertTrue(Scalars.lessEquals(RealScalar.ZERO, ate.getDistancElement().totalDistanceWtCst));
        scalarAssert.add((Scalar) RealScalar.of(109712.05545).map(Round._5), (Scalar) ate.getDistancElement().totalDistanceWtCst.map(Round._5));
        assertTrue(Scalars.lessEquals(RealScalar.ZERO, ate.getDistancElement().totalDistancePicku));
        scalarAssert.add((Scalar) RealScalar.of(13408.35267).map(Round._5), (Scalar) ate.getDistancElement().totalDistancePicku.map(Round._5));
        assertTrue(Scalars.lessEquals(RealScalar.ZERO, ate.getDistancElement().totalDistanceRebal));
        scalarAssert.add((Scalar) RealScalar.of(215942.24571).map(Round._5), (Scalar) ate.getDistancElement().totalDistanceRebal.map(Round._5));
        assertTrue(Scalars.lessEquals(RealScalar.ZERO, ate.getDistancElement().totalDistanceRatio));
        scalarAssert.add((Scalar) RealScalar.of(0.32357).map(Round._5), (Scalar) ate.getDistancElement().totalDistanceRatio.map(Round._5));
        scalarAssert.consolidate();

        ate.getDistancElement().totalDistancesPerVehicle.flatten(-1).forEach(s -> //
        assertTrue(Scalars.lessEquals(RealScalar.ZERO, (Scalar) s)));
        assertTrue(((Scalar) Total.of(ate.getDistancElement().totalDistancesPerVehicle)).equals( //
                ate.getDistancElement().totalDistance));
        assertTrue(((Scalar) Total.of(ate.getDistancElement().totalDistancesPerVehicle)).equals( //
                ate.getDistancElement().totalDistance));

        /** waiting Times */
        assertTrue(Scalars.lessEquals(Quantity.of(0, SI.SECOND), ate.getTravelTimeAnalysis().getWaitAggrgte().Get(2)));
        ate.getTravelTimeAnalysis().getWaitTimes().flatten(-1).forEach(t -> {
            Scalars.lessEquals(Quantity.of(0, SI.SECOND), (Scalar) t);
            Scalars.lessEquals((Scalar) t, ate.getTravelTimeAnalysis().getWaitAggrgte().Get(2));

        });

        assertTrue(Scalars.lessEquals(Quantity.of(0, SI.SECOND), ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(0)));
        assertTrue(Scalars.lessEquals(ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(0), ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(1)));
        assertTrue(Scalars.lessEquals(ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(1), ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(2)));
        assertTrue(Scalars.lessEquals(Quantity.of(0, SI.SECOND), ate.getTravelTimeAnalysis().getWaitAggrgte().Get(1)));

        /** presence of plot files */
        File data = new File("output/001/data");
        assertTrue(new File(data, "binnedWaitingTimes.png").exists());
        assertTrue(new File(data, "distanceDistribution.png").exists());
        assertTrue(new File(data, "occAndDistRatios.png").exists());
        assertTrue(new File(data, "stackedDistance.png").exists());
        assertTrue(new File(data, "statusDistribution.png").exists());
        assertTrue(new File(data, AnalysisConstants.ParametersExportFilename).exists());
        assertTrue(new File(data, "WaitingTimes").isDirectory());
        assertTrue(new File(data, "WaitingTimes/WaitingTimes.mathematica").exists());
        assertTrue(new File(data, "StatusDistribution").isDirectory());
        assertTrue(new File(data, "StatusDistribution/StatusDistribution.mathematica").exists());
        assertTrue(new File(data, "DistancesOverDay").isDirectory());
        assertTrue(new File(data, "DistancesOverDay/DistancesOverDay.mathematica").exists());
        assertTrue(new File(data, "DistanceRatios").isDirectory());
        assertTrue(new File(data, "DistanceRatios/DistanceRatios.mathematica").exists());
        assertTrue(new File("output/001/report/report.html").exists());
        assertTrue(new File("output/001/report/av.xml").exists());
        assertTrue(new File("output/001/report/config.xml").exists());
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }
}
