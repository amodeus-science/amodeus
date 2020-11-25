/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.matsim.api.core.v01.network.Network;

import amodeus.amodeus.analysis.AnalysisConstants;
import amodeus.amodeus.analysis.StackedDistanceChartImage;
import amodeus.amodeus.analysis.element.BinnedWaitingTimesImage;
import amodeus.amodeus.analysis.element.DistanceDistributionOverDayImage;
import amodeus.amodeus.analysis.element.NumberPassengersAnalysis;
import amodeus.amodeus.analysis.element.OccupancyDistanceRatiosImage;
import amodeus.amodeus.analysis.element.StatusDistributionImage;
import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.options.ScenarioOptionsBase;
import amodeus.amodeus.testutils.SharedTestServer;
import amodeus.amodeus.testutils.TestPreparer;
import amodeus.amodeus.util.io.CopyFiles;
import amodeus.amodeus.util.io.Locate;
import amodeus.amodeus.util.io.MultiFileTools;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.util.math.SI;
import amodeus.amodeus.util.matsim.NetworkLoader;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.red.Total;
import ch.ethz.idsc.tensor.sca.Round;

@FixMethodOrder(MethodSorters.NAME_ASCENDING) // junit 5 would provide more elegant solution
public class SharedScenarioExecutionTest {
    private static final Scalar ZERO_KM = Quantity.of(0, "km");
    // ---
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
                Collections.singletonList("LPOptions.properties"), true);

        testServer = new SharedTestServer(workingDirectory);
    }

    @Test
    public void testA_Preparer() throws Exception {
        System.out.print("Preparer Test:\t");

        // run scenario preparer
        TestPreparer testPreparer = TestPreparer.run(testServer.getWorkingDirectory());

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
    public void testB_Server() throws Exception {
        System.out.print("Server Test:\t");

        // run scenario server
        testServer.simulate();

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
    public void testC_Analysis() throws Exception {
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

        ScalarAssert scalarAssert = new ScalarAssert();

        /** fleet distances */
        assertTrue(Scalars.lessEquals(ZERO_KM, ate.getDistancElement().totalDistance));
        scalarAssert.add((Scalar) Quantity.of(1337.59378, "km").map(Round._5), (Scalar) ate.getDistancElement().totalDistance.map(Round._5));
        assertTrue(Scalars.lessEquals(ZERO_KM, ate.getDistancElement().totalDistanceWtCst));
        scalarAssert.add((Scalar) Quantity.of(1043.80037, "km").map(Round._5), (Scalar) ate.getDistancElement().totalDistanceWtCst.map(Round._5));
        assertTrue(Scalars.lessEquals(ZERO_KM, ate.getDistancElement().totalDistancePicku));
        scalarAssert.add((Scalar) Quantity.of(293.79340, "km").map(Round._5), (Scalar) ate.getDistancElement().totalDistancePicku.map(Round._5));
        assertTrue(Scalars.lessEquals(ZERO_KM, ate.getDistancElement().totalDistanceRebal));
        scalarAssert.add((Scalar) Quantity.of(0.00000, "km").map(Round._5), (Scalar) ate.getDistancElement().totalDistanceRebal.map(Round._5));
        assertTrue(Scalars.lessEquals(RealScalar.ZERO, ate.getDistancElement().totalDistanceRatio));
        scalarAssert.add((Scalar) RealScalar.of(0.78036).map(Round._5), (Scalar) ate.getDistancElement().totalDistanceRatio.map(Round._5));
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
        NumberPassengersAnalysis npa = ate.getNumberPassengersAnalysis();
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
