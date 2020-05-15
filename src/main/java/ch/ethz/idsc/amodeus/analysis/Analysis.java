/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.tensor.qty.Unit;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisElement;
import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.BinnedWaitingTimesImage;
import ch.ethz.idsc.amodeus.analysis.element.DistanceDistributionOverDayImage;
import ch.ethz.idsc.amodeus.analysis.element.DriveTimeHtml;
import ch.ethz.idsc.amodeus.analysis.element.OccupancyDistanceRatiosImage;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionImage;
import ch.ethz.idsc.amodeus.analysis.element.TotalJourneyTimeHtml;
import ch.ethz.idsc.amodeus.analysis.element.TravelTimeExport;
import ch.ethz.idsc.amodeus.analysis.element.WaitTimeHtml;
import ch.ethz.idsc.amodeus.analysis.element.WaitingCustomerExport;
import ch.ethz.idsc.amodeus.analysis.plot.ChartTheme;
import ch.ethz.idsc.amodeus.analysis.plot.ColorDataAmodeus;
import ch.ethz.idsc.amodeus.analysis.report.AnalysisReport;
import ch.ethz.idsc.amodeus.analysis.report.DistanceElementHtml;
import ch.ethz.idsc.amodeus.analysis.report.FleetEfficiencyHtml;
import ch.ethz.idsc.amodeus.analysis.report.HtmlReport;
import ch.ethz.idsc.amodeus.analysis.report.HtmlReportElement;
import ch.ethz.idsc.amodeus.analysis.report.ScenarioParametersHtml;
import ch.ethz.idsc.amodeus.analysis.report.SimulationInformationHtml;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TotalValues;
import ch.ethz.idsc.amodeus.analysis.report.TtlValIdent;
import ch.ethz.idsc.amodeus.analysis.shared.NumberPassengerStatusDistribution;
import ch.ethz.idsc.amodeus.analysis.shared.RideSharingDistributionCompositionStack;
import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.StorageSupplier;
import ch.ethz.idsc.amodeus.net.StorageUtils;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.io.Timing;

public class Analysis {

    /** returns an Instance of the Analysis Class can be called with any combination
     * of null and the respective parameter(s).
     * 
     * @param workingDirectory
     *            default: current working directory. Is the file where the config
     *            file, AmodeusOptions file and the outputfolder are located
     * @param outputDirectory:
     *            default: value stored in the Simulation Config file. Can be
     *            changed if for example an other outputfolder from the Sequential
     *            Server has to be analysed.
     * @param network:
     *            default: Network defined in the Config file. Can be used to reduce
     *            runtime if the Network was already loaded in a previous step (e.g.
     *            Scenario Server)
     * @throws Exception */
    public static Analysis setup(ScenarioOptions scenarioOptions, File outputDirectory, //
            Network network, MatsimAmodeusDatabase db) throws Exception {
        return new Analysis(scenarioOptions, outputDirectory, network, db);
    }

    // List of Analysis Elements which will be loaded
    private final static String DATAFOLDERNAME = "data";
    // ---
    private final List<AnalysisElement> analysisElements = new LinkedList<>();
    private final List<AnalysisExport> analysisExports = new LinkedList<>();
    private final List<AnalysisReport> analysisReports = new LinkedList<>();

    private final File dataDirectory;
    private final StorageSupplier storageSupplier;
    private final int size;
    private final AnalysisSummary analysisSummary;
    private final HtmlReport htmlReport;
    private final TotalValues totalValues;
    private final ColorDataIndexed colorDataIndexed;
    // private final StandardChartTheme chartTheme; // <- not used
    private final Set<String> allAmodeusTotalValueIdentifiers = TtlValIdent.getAllIdentifiers();

    /** Constructor of the Analysis Class can be called with any combination of null
     * and the respective parameter.
     * 
     * @param workingDirectory
     *            default: current working directory. Is the file where the config
     *            file, AmodeusOptions file and the outputfolder are located
     * @param outputDirectory:
     *            default: value stored in the Simulation Config file. Can be
     *            changed if for example an other outputfolder from the Sequential
     *            Server has to be analysed.
     * @param network:
     *            default: Network defined in the Config file. Can be used to reduce
     *            runtime if the Network was already loaded in a previous step (e.g.
     *            Scenario Server)
     * @throws Exception */

    private Analysis(ScenarioOptions scenarioOptions, File outputDirectory, //
            Network network, MatsimAmodeusDatabase db) throws Exception {
        if (Objects.isNull(scenarioOptions))
            throw new RuntimeException("Analysis requires a ScenarioOptions object as input.");
        Objects.requireNonNull(scenarioOptions.getWorkingDirectory());
        File workingDirectory = scenarioOptions.getWorkingDirectory();
        File configFile = new File(scenarioOptions.getSimulationConfigName());
        Objects.requireNonNull(configFile);

        // if (Objects.isNull(workingDirectory) || !workingDirectory.isDirectory())
        // workingDirectory = new File("").getCanonicalFile();
        System.out.println("workingDirectory in Analysis: " + workingDirectory.getAbsolutePath());
        ScenarioOptions scenOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        if (outputDirectory == null || !outputDirectory.isDirectory()) {
            Config config = ConfigUtils.loadConfig(configFile.toString());
            String outputDirectoryName = config.controler().getOutputDirectory();
            outputDirectory = new File(workingDirectory, outputDirectoryName);
        }
        System.out.println("Outputdirectory chosen in Analysis: " + outputDirectory.getAbsolutePath());

        if (Objects.isNull(network))
            network = NetworkLoader.fromConfigFile(configFile);

        // load colorScheme & theme
        colorDataIndexed = ColorDataAmodeus.indexed(scenOptions.getColorScheme());

        // chartTheme was not used
        // chartTheme = ChartTheme.valueOf(scenOptions.getChartTheme());

        ChartFactory.setChartTheme(ChartTheme.STANDARD);
        BarRenderer.setDefaultBarPainter(new StandardBarPainter());
        BarRenderer.setDefaultShadowsVisible(false);

        outputDirectory.mkdir();
        dataDirectory = new File(outputDirectory, DATAFOLDERNAME);
        dataDirectory.mkdir();

        // load simulation data
        StorageUtils storageUtils = new StorageUtils(outputDirectory);
        storageUtils.printStorageProperties();
        storageSupplier = new StorageSupplier(storageUtils.getFirstAvailableIteration());
        size = storageSupplier.size();
        System.out.println("Found files: " + size);
        Set<Integer> vehicleIndices = storageSupplier.getSimulationObject(1).vehicles.stream().map(vc -> vc.vehicleIndex).collect(Collectors.toSet());

        analysisSummary = new AnalysisSummary(vehicleIndices, db, scenarioOptions);

        // default List of Analysis Elements which will be loaded
        analysisElements.add(analysisSummary.getSimulationInformationElement());
        analysisElements.add(analysisSummary.getStatusDistribution());
        analysisElements.add(analysisSummary.getDistanceElement());
        analysisElements.add(analysisSummary.getTravelTimeAnalysis());
        analysisElements.add(analysisSummary.getNumberPassengersAnalysis());

        analysisExports.add(BinnedWaitingTimesImage.INSTANCE);
        analysisExports.add(DistanceDistributionOverDayImage.INSTANCE);
        analysisExports.add(OccupancyDistanceRatiosImage.INSTANCE);
        analysisExports.add(StackedDistanceChartImage.INSTANCE);
        analysisExports.add(StatusDistributionImage.INSTANCE);
        analysisExports.add(NumberPassengerStatusDistribution.INSTANCE);
        analysisExports.add(RideSharingDistributionCompositionStack.INSTANCE);
        analysisExports.add(ScenarioParametersExport.INSTANCE);
        analysisExports.add(WaitTimeHistoImage.INSTANCE);
        analysisExports.add(DriveTimeImages.INSTANCE);
        analysisExports.add(TotalJourneyTimeImage.INSTANCE);

        analysisExports.add(DistancesOverDayTable.INSTANCE);
        analysisExports.add(DistancesRatiosTable.INSTANCE);
        analysisExports.add(WaitingTimesTable.INSTANCE);
        analysisExports.add(StatusDistributionTable.INSTANCE);
        analysisExports.add(new VirtualNetworkExport(scenOptions));
        analysisExports.add(TravelTimeExport.INSTANCE);
        analysisExports.add(WaitingCustomerExport.INSTANCE);

        // default list of analysis reports
        htmlReport = new HtmlReport(outputDirectory, scenOptions);
        htmlReport.addHtmlReportElement(ScenarioParametersHtml.INSTANCE);
        htmlReport.addHtmlReportElement(SimulationInformationHtml.INSTANCE);
        htmlReport.addHtmlReportElement(DistanceElementHtml.INSTANCE);
        htmlReport.addHtmlReportElement(WaitTimeHtml.INSTANCE);
        htmlReport.addHtmlReportElement(DriveTimeHtml.INSTANCE);
        htmlReport.addHtmlReportElement(TotalJourneyTimeHtml.INSTANCE);
        htmlReport.addHtmlReportElement(FleetEfficiencyHtml.INSTANCE);

        analysisReports.add(htmlReport);

        totalValues = new TotalValues(dataDirectory);
        totalValues.append(analysisSummary.getScenarioParameters());
        totalValues.append(analysisSummary.getSimulationInformationElement());
        totalValues.append(analysisSummary.getStatusDistribution());
        totalValues.append(analysisSummary.getTravelTimeAnalysis());
        totalValues.append(analysisSummary.getDistanceElement());
        totalValues.append(analysisSummary.getNumberPassengersAnalysis());

        analysisReports.add(totalValues);

    }

    public void addAnalysisElement(AnalysisElement analysisElement) {
        analysisElements.add(analysisElement);
    }

    public void addAnalysisExport(AnalysisExport analysisExport) {
        analysisExports.add(analysisExport);
    }

    public void addAnalysisReport(AnalysisReport analysisReport) {
        analysisReports.add(analysisReport);
    }

    public void addHtmlElement(HtmlReportElement htmlReportElement) {
        htmlReport.addHtmlReportElement(htmlReportElement);
    }

    public void addTotalValue(TotalValueAppender totalValueAppender) {
        for (TotalValueIdentifier totalValueIdentifier : totalValueAppender.getTotalValues().keySet()) {
            // Check that from outside no AModeus Identifier is used
            // If it fails here you have to change your String in your total Value Identifier-
            GlobalAssert.that(!allAmodeusTotalValueIdentifiers.contains(totalValueIdentifier.getIdentifier()));
        }
        totalValues.append(totalValueAppender);
    }

    public AnalysisSummary getAnalysisSummary() {
        return analysisSummary;
    }

    public void run() throws Exception {
        /** iterate simulation objects */
        {
            Timing timing = Timing.started();
            for (int index = 0; index < size; ++index) {
                SimulationObject simulationObject = storageSupplier.getSimulationObject(index);
                analysisElements.forEach(analysisElement -> analysisElement.register(simulationObject));
                if (simulationObject.now % 10_000 == 0)
                    System.out.println(String.format("%6.2f now=%d", timing.seconds(), simulationObject.now));
            }
            System.out.println(String.format("%6.2f register all", timing.seconds()));
        }

        /** this tep includes processing after all time steps are loaded */
        {
            Timing timing = Timing.started();
            analysisElements.forEach(AnalysisElement::consolidate);
            System.out.println(String.format("%6.2f consolidate all", timing.seconds()));
        }

        for (AnalysisExport analysisExport : analysisExports) {
            Timing timing = Timing.started();
            analysisExport.summaryTarget(analysisSummary, dataDirectory, colorDataIndexed);
            System.out.println(String.format("%6.2f %s", timing.seconds(), analysisExport.getClass().getSimpleName()));
        }

        /** generate reports */
        {
            Timing timing = Timing.started();
            analysisReports.forEach(analysisReport -> analysisReport.generate(analysisSummary));
            System.out.println(String.format("%6.2f generate all", timing.seconds()));
        }
    }

    public void setDistanceUnit(String unit) {
        DistanceElement.setDistanceUnit(unit);
    }

    public void setDistanceUnit(Unit unit) {
        DistanceElement.setDistanceUnit(unit);
    }
}
