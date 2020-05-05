/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.analysis.AnalysisConstants;
import ch.ethz.idsc.amodeus.analysis.StackedDistanceChartImage;
import ch.ethz.idsc.amodeus.analysis.element.BinnedWaitingTimesImage;
import ch.ethz.idsc.amodeus.analysis.element.DistanceDistributionOverDayImage;
import ch.ethz.idsc.amodeus.analysis.element.NumberPassengersAnalysis;
import ch.ethz.idsc.amodeus.analysis.element.OccupancyDistanceRatiosImage;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionImage;
import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.testutils.SharedTestServer;
import ch.ethz.idsc.amodeus.testutils.TestPreparer;
import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Total;
import ch.ethz.idsc.tensor.sca.Round;

public class SharedScenarioExecutionTest {
    private static final Scalar ZERO_KM = Quantity.of(0, "km");
    // ---
    private static TestPreparer testPreparer;
    private static SharedTestServer testServer;

    @BeforeClass
    public static void setUpOnce() throws Exception {
        // copy scenario data into main directory
        File scenarioDirectory = new File(Locate.repoFolder(SharedScenarioExecutionTest.class, "amodeus"), "resources/testScenario");
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        GlobalAssert.that(workingDirectory.isDirectory());
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath());

        // copy LPOptions from other location to ensure no travel data object is created,
        // the dispatcher used in this test does not require it.
        File helperDirectory = //
                new File(Locate.repoFolder(ScenarioExecutionTest.class, "amodeus"), "resources/helperFiles");
        CopyFiles.now(helperDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath(), //
                Arrays.asList("LPOptions.properties"), true);

        // run scenario preparer
        testPreparer = TestPreparer.run(workingDirectory);

        // run scenario server
        testServer = SharedTestServer.run(workingDirectory);

        Map<String, Link> map = new HashMap<>();
        testPreparer.getPreparedNetwork().getLinks().forEach((k, v) -> map.put(k.toString(), v));
    }

    @Test
    public void testPreparer() throws Exception {
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
        /** scenario options */
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        assertEquals(new File(workingDirectory, "config.xml").getAbsolutePath(), scenarioOptions.getSimulationConfigName());
        assertEquals(new File(workingDirectory, "preparedNetwork").getAbsolutePath(), scenarioOptions.getPreparedNetworkName());
        assertEquals(new File(workingDirectory, "preparedPopulation").getAbsolutePath(), scenarioOptions.getPreparedPopulationName());

        /** simulation objects should exist after simulation (simulation data) */
        File simobj = new File("output/001/simobj/it.00");
        assertTrue(simobj.exists());
        assertEquals(109, simobj.listFiles().length);
        assertTrue(new File(simobj, "0108000/0108000.bin").exists());
        assertTrue(new File(simobj, "0000000/0000120.bin").exists());
    }

    @Test
    public void testAnalysis() throws Exception {
        System.out.print("Analysis Test:\t");

        AnalysisTestExport ate = testServer.getAnalysisTestExport();

        /** number of processed requests, 25 of the population are same-link trips that are not passed on to Amodeus */
        assertEquals(247, ate.getSimulationInformationElement().reqsize());

        /** fleet size */
        assertEquals(40, ate.getSimulationInformationElement().vehicleSize());

        /** status distribution, every row must equal the total of vehicles */
        Tensor distributionSum = Total.of(Transpose.of(ate.getStatusDistribution().statusTensor));
        distributionSum.flatten(-1).forEach(e -> //
        assertEquals(RealScalar.of(ate.getSimulationInformationElement().vehicleSize()), e));

        /** distance and occupancy ratios */
        Scalar occupancyRatio = Mean.of(ate.getDistancElement().distanceRatioOverDay).Get(0);
        Scalar distanceRatio = Mean.of(ate.getDistancElement().distanceRatioOverDay).Get(1);

        ScalarAssert scalarAssert = new ScalarAssert();

        scalarAssert.add((Scalar) RealScalar.of(0.04994).map(Round._5), (Scalar) occupancyRatio.map(Round._5));
        scalarAssert.add((Scalar) RealScalar.of(0.554888423).map(Round._5), (Scalar) distanceRatio.map(Round._5));

        /** fleet distances */
        assertTrue(Scalars.lessEquals(ZERO_KM, ate.getDistancElement().totalDistance));
        scalarAssert.add((Scalar) Quantity.of(1347.53192, "km").map(Round._5), (Scalar) ate.getDistancElement().totalDistance.map(Round._5));
        assertTrue(Scalars.lessEquals(ZERO_KM, ate.getDistancElement().totalDistanceWtCst));
        scalarAssert.add((Scalar) Quantity.of(1072.53309, "km").map(Round._5), (Scalar) ate.getDistancElement().totalDistanceWtCst.map(Round._5));
        assertTrue(Scalars.lessEquals(ZERO_KM, ate.getDistancElement().totalDistancePicku));
        scalarAssert.add((Scalar) Quantity.of(274.99884, "km").map(Round._5), (Scalar) ate.getDistancElement().totalDistancePicku.map(Round._5));
        assertTrue(Scalars.lessEquals(ZERO_KM, ate.getDistancElement().totalDistanceRebal));
        scalarAssert.add((Scalar) Quantity.of(0.00000, "km").map(Round._5), (Scalar) ate.getDistancElement().totalDistanceRebal.map(Round._5));
        assertTrue(Scalars.lessEquals(RealScalar.ZERO, ate.getDistancElement().totalDistanceRatio));
        scalarAssert.add((Scalar) RealScalar.of(0.79592).map(Round._5), (Scalar) ate.getDistancElement().totalDistanceRatio.map(Round._5));
        scalarAssert.consolidate();

        ate.getDistancElement().totalDistancesPerVehicle.flatten(-1).forEach(s -> //
        assertTrue(Scalars.lessEquals(ZERO_KM, (Scalar) s)));
        assertEquals(Total.of(ate.getDistancElement().totalDistancesPerVehicle), ate.getDistancElement().totalDistance);

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

        /** number of passengers */
        NumberPassengersAnalysis npa = testServer.numberPassengersAnalysis();
        assertEquals(Total.of(npa.getSharedOthersDistribution()).Get().number().intValue(), npa.getSharedOthersPerRequest().length());

        /** presence of plot files */
        File data = new File("output/001/data");
        assertTrue(new File(data, BinnedWaitingTimesImage.FILE_PNG).isFile());
        assertTrue(new File(data, DistanceDistributionOverDayImage.FILE_PNG).isFile());
        assertTrue(new File(data, OccupancyDistanceRatiosImage.FILE_PNG).isFile());
        assertTrue(new File(data, StackedDistanceChartImage.FILE_PNG).isFile());
        assertTrue(new File(data, StatusDistributionImage.FILE_PNG).isFile());
        assertTrue(new File(data, AnalysisConstants.ParametersExportFilename).isFile());
        assertTrue(new File(data, "WaitingTimes").isDirectory());
        assertTrue(new File(data, "WaitingTimes/WaitingTimes.mathematica").isFile());
        assertTrue(new File(data, "StatusDistribution").isDirectory());
        assertTrue(new File(data, "StatusDistribution/StatusDistribution.mathematica").isFile());
        assertTrue(new File(data, "DistancesOverDay").isDirectory());
        assertTrue(new File(data, "DistancesOverDay/DistancesOverDay.mathematica").isFile());
        assertTrue(new File(data, "DistanceRatios").isDirectory());
        assertTrue(new File(data, "DistanceRatios/DistanceRatios.mathematica").isFile());
        assertTrue(new File("output/001/report/report.html").isFile());
        assertTrue(new File("output/001/report/config.xml").isFile());
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }
}
