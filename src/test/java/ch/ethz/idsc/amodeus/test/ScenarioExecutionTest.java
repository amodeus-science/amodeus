/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.analysis.AnalysisConstants;
import ch.ethz.idsc.amodeus.analysis.StackedDistanceChartImage;
import ch.ethz.idsc.amodeus.analysis.element.BinnedWaitingTimesImage;
import ch.ethz.idsc.amodeus.analysis.element.DistanceDistributionOverDayImage;
import ch.ethz.idsc.amodeus.analysis.element.OccupancyDistanceRatiosImage;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionImage;
import ch.ethz.idsc.amodeus.analysis.element.TravelHistory;
import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.testutils.TestPreparer;
import ch.ethz.idsc.amodeus.testutils.TestServer;
import ch.ethz.idsc.amodeus.testutils.TestViewer;
import ch.ethz.idsc.amodeus.util.io.CopyFiles;
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
import ch.ethz.idsc.tensor.io.UserName;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.qty.UnitConvert;
import ch.ethz.idsc.tensor.red.Total;
import ch.ethz.idsc.tensor.sca.Round;

public class ScenarioExecutionTest {
    private static final Scalar ZERO_KM = Quantity.of(0, "km");
    // ---
    private static TestPreparer testPreparer;
    private static TestServer testServer;
    private static TestViewer testViewer;

    @BeforeClass
    public static void setUpOnce() throws Exception {

        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();

        // copy scenario data into main directory
        File scenarioDirectory = //
                new File(Locate.repoFolder(ScenarioExecutionTest.class, "amodeus"), "resources/testScenario");
        GlobalAssert.that(workingDirectory.isDirectory());
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), //
                workingDirectory.getAbsolutePath());

        // copy LPOptions from other location to ensure no travel data object is created,
        // the dispatcher used in this test does not require it.
        File helperDirectory = //
                new File(Locate.repoFolder(ScenarioExecutionTest.class, "amodeus"), "resources/helperFiles");
        CopyFiles.now(helperDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath(), //
                Arrays.asList("LPOptions.properties"), true);

        // run scenario preparer
        testPreparer = TestPreparer.run(workingDirectory);

        // run scenario server
        testServer = TestServer.run().on(workingDirectory);

        // run scenario viewer
        if (!UserName.is("travis"))
            testViewer = TestViewer.run(workingDirectory);
    }

    @Test
    public void testPreparer() throws Exception {
        System.out.print("Preparer Test:\t");

        // creation of files
        File preparedPopulationFile = new File("preparedPopulation.xml");
        assertTrue(preparedPopulationFile.isFile());

        File preparedNetworkFile = new File("preparedNetwork.xml");
        assertTrue(preparedNetworkFile.isFile());

        File config = new File("config.xml");
        assertTrue(config.isFile());

        // consistency of network (here no cutting)
        Network originalNetwork = NetworkLoader.fromConfigFile(testServer.getConfigFile());
        Network preparedNetwork = testPreparer.getPreparedNetwork();
        GlobalAssert.that(Objects.nonNull(originalNetwork));
        GlobalAssert.that(Objects.nonNull(preparedNetwork));
        assertEquals(preparedNetwork.getNodes().size(), originalNetwork.getNodes().size());
        assertEquals(preparedNetwork.getLinks().size(), originalNetwork.getLinks().size());

        // consistency of population
        Population population = testPreparer.getPreparedPopulation();
        assertEquals(250, population.getPersons().size());
    }

    @Test
    public void testServer() throws Exception {
        System.out.print("Server Test:\t");

        // scenario options
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());

        assertEquals(new File(workingDirectory, "config.xml").getAbsolutePath(), scenarioOptions.getSimulationConfigName());
        assertEquals(new File(workingDirectory, "preparedNetwork").getAbsolutePath(), scenarioOptions.getPreparedNetworkName());
        assertEquals(new File(workingDirectory, "preparedPopulation").getAbsolutePath(), scenarioOptions.getPreparedPopulationName());

        // simulation objects should exist after simulation (simulation data)
        File simobj = new File("output/001/simobj/it.00");
        assertTrue(simobj.exists());
        assertEquals(109, simobj.listFiles().length);
        assertTrue(new File(simobj, "0000000/0000120.bin").exists());
        assertTrue(new File(simobj, "0108000/0108000.bin").exists());
    }

    @Test
    public void testAnalysis() throws Exception {
        System.out.print("Analysis Test:\t");

        AnalysisTestExport ate = testServer.getAnalysisTestExport();

        /** number of processed requests, 25 are same link requests and therefore are not covered by Amodeus */
        assertEquals(247, ate.getSimulationInformationElement().reqsize());

        /** fleet size */
        assertEquals(40, ate.getSimulationInformationElement().vehicleSize());
        assertEquals(40, ate.getDistancElement().getVehicleStatistics().size());

        /** status distribution, every row must equal the total of vehicles */
        Tensor distributionSum = Total.of(Transpose.of(ate.getStatusDistribution().statusTensor));
        distributionSum.flatten(-1).forEach(e -> //
        assertEquals(e, RealScalar.of(ate.getSimulationInformationElement().vehicleSize())));

        ScalarAssert scalarAssert = new ScalarAssert();

        /** fleet distances */
        assertTrue(Scalars.lessEquals(ZERO_KM, ate.getDistancElement().totalDistance));
        assertTrue(Scalars.lessEquals(ZERO_KM, ate.getDistancElement().totalDistanceWtCst));
        assertTrue(Scalars.lessEquals(ZERO_KM, ate.getDistancElement().totalDistancePicku));
        assertTrue(Scalars.lessEquals(ZERO_KM, ate.getDistancElement().totalDistanceRebal));
        assertTrue(Scalars.lessEquals(RealScalar.ZERO, ate.getDistancElement().totalDistanceRatio));
        ate.getDistancElement().totalDistancesPerVehicle.flatten(-1).forEach(s -> //
        assertTrue(Scalars.lessEquals(ZERO_KM, (Scalar) s)));
        assertEquals(Total.of(ate.getDistancElement().totalDistancesPerVehicle), ate.getDistancElement().totalDistance);

        scalarAssert.add((Scalar) Quantity.of(2868.91644, "km").map(Round._5), (Scalar) ate.getDistancElement().totalDistance.map(Round._5));
        scalarAssert.add((Scalar) Quantity.of(1061.37873, "km").map(Round._5), (Scalar) ate.getDistancElement().totalDistanceWtCst.map(Round._5));
        scalarAssert.add((Scalar) Quantity.of(252.24140, "km").map(Round._5), (Scalar) ate.getDistancElement().totalDistancePicku.map(Round._5));
        scalarAssert.add((Scalar) Quantity.of(1462.21728, "km").map(Round._5), (Scalar) ate.getDistancElement().totalDistanceRebal.map(Round._5));
        scalarAssert.add((Scalar) RealScalar.of(0.36996).map(Round._5), (Scalar) ate.getDistancElement().totalDistanceRatio.map(Round._5));

        scalarAssert.add((Scalar) Total.of(ate.getDistancElement().totalDistancesPerVehicle), //
                ate.getDistancElement().totalDistance);

        /** travel time history */
        assertEquals(247, ate.getTravelTimeAnalysis().getTravelHistories().size());
        assertTrue(ate.getTravelTimeAnalysis().getTravelHistories().values().stream().map(TravelHistory::getDropOffTime) //
                .allMatch(s -> Scalars.lessEquals(s, UnitConvert.SI().to(SI.SECOND).apply(Quantity.of(30, "h")))));

        /** wait times, drive times */
        assertTrue(Scalars.lessEquals(Quantity.of(0, SI.SECOND), ate.getTravelTimeAnalysis().getWaitAggrgte().Get(2)));
        ate.getTravelTimeAnalysis().getWaitTimes().flatten(-1).forEach(t -> {
            Scalars.lessEquals(Quantity.of(0, SI.SECOND), (Quantity) t);
            Scalars.lessEquals((Quantity) t, ate.getTravelTimeAnalysis().getWaitAggrgte().Get(2));
        });

        assertTrue(Scalars.lessEquals(Quantity.of(0, SI.SECOND), ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(0)));
        assertTrue(Scalars.lessEquals(ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(0), //
                ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(1)));
        assertTrue(Scalars.lessEquals(ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(1), //
                ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(2)));
        assertTrue(Scalars.lessEquals(Quantity.of(0, SI.SECOND), ate.getTravelTimeAnalysis().getWaitAggrgte().Get(1)));

        scalarAssert.add(Quantity.of(379.97975708502025, SI.SECOND), ate.getTravelTimeAnalysis().getWaitAggrgte().Get(1));
        scalarAssert.add(Quantity.of(1943.0, SI.SECOND), ate.getTravelTimeAnalysis().getWaitAggrgte().Get(2));
        scalarAssert.add(Quantity.of(RationalScalar.of(215040, 247), SI.SECOND), ate.getTravelTimeAnalysis().getDrveAggrgte().Get(1));
        scalarAssert.add(Quantity.of(3000, SI.SECOND), ate.getTravelTimeAnalysis().getDrveAggrgte().Get(2));

        /* TODO @sebhoerl Have a look at {AmodeusModule::install}. At some point the travel time
         * calculation in DVRP has been improved.
         * Unfortunately, this improvement breaks these tests.
         * The reference numbers here should be adjusted at some point so that the fallback in
         * {AmodeusModule::install} can be removed again.
         * (Nevertheless, we're talking about a different in routed time of +/-1 second). /sebhoerl */
        scalarAssert.consolidate();

        /** presence of plot files */
        File data = new File("output/001/data");
        assertTrue(new File(data, BinnedWaitingTimesImage.FILE_PNG).isFile());
        assertTrue(new File(data, DistanceDistributionOverDayImage.FILE_PNG).isFile());
        assertTrue(new File(data, OccupancyDistanceRatiosImage.FILE_PNG).isFile());
        assertTrue(new File(data, StackedDistanceChartImage.FILE_PNG).isFile());
        assertTrue(new File(data, StatusDistributionImage.FILE_PNG).isFile());

        assertTrue(new File(data, AnalysisConstants.ParametersExportFilename).exists());

        assertTrue(new File(data, "WaitingTimes").isDirectory());
        assertTrue(new File(data, "WaitingTimes/WaitingTimes.mathematica").isFile());

        assertTrue(new File(data, "StatusDistribution").isDirectory());
        assertTrue(new File(data, "StatusDistribution/StatusDistribution.mathematica").isFile());

        assertTrue(new File(data, "DistancesOverDay").isDirectory());
        assertTrue(new File(data, "DistancesOverDay/DistancesOverDay.mathematica").isFile());

        assertTrue(new File(data, "DistanceRatios").isDirectory());
        assertTrue(new File(data, "DistanceRatios/DistanceRatios.mathematica").isFile());

        assertTrue(new File("output/001/data/requestHistory.csv").isFile());
        assertTrue(new File("output/001/data/vehicleHistory.csv").isFile());

        assertTrue(new File("output/001/report/report.html").isFile());
        assertTrue(new File("output/001/report/config.xml").isFile());
    }

    @Test
    public void testViewer() {
        if (!UserName.is("travis")) {
            System.out.println("Viewer Test:");

            assertEquals(9, testViewer.getAmodeusComponent().viewerLayers.size());
            assertEquals(31, testViewer.getViewerConfig().settings.getClass().getFields().length);
        }
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }
}
