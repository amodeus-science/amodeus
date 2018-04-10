/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisElement;
import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.plot.ChartTheme;
import ch.ethz.idsc.amodeus.analysis.plot.ColorScheme;
import ch.ethz.idsc.amodeus.analysis.report.AnalysisReport;
import ch.ethz.idsc.amodeus.analysis.report.HtmlReport;
import ch.ethz.idsc.amodeus.analysis.report.HtmlReportElement;
import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.StorageSupplier;
import ch.ethz.idsc.amodeus.net.StorageUtils;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;

public class Analysis {
    /** Use this method to create an standalone Analysis with all the default values stored in the current Working Directory
     * 
     * @return
     * @throws Exception */
    public static Analysis setup() throws Exception {
        return setup(null, null, null, null);
    }

    /** Use this method in the Simulation Server as the network was already loaded
     * 
     * @param network
     * @return
     * @throws Exception */
    public static Analysis setup(Network network) throws Exception {
        return setup(null, null, null, network);
    }

    public static Analysis setup(File workingDirectory, File configFile, File outputDirectory) throws Exception {
        return new Analysis(workingDirectory, configFile, outputDirectory, null);
    }

    /** returns an Instance of the Analysis Class can be called with any combination of null and the respective parameter(s).
     * 
     * @param workingDirectory default: current working directory. Is the file where the config file, AmodeusOptions file and the outputfolder are located
     * @param configFile default: SimulationConfig file as defined in AmodeusOptions. Stores the data of the corresponding outputdirectory and Network.
     * @param outputDirectory: default: value stored in the Simulation Config file. Can be changed if for example an other outputfolder from the Sequential
     *            Server
     *            has to be analysed.
     * @param network: default: Network defined in the Config file. Can be used to reduce runtime if the Network was already loaded in a previous step (e.g.
     *            Scenario Server)
     * @throws Exception */
    public static Analysis setup(File workingDirectory, File configFile, File outputDirectory, Network network) throws Exception {
        return new Analysis(workingDirectory, configFile, outputDirectory, network);
    }

    // List of Analysis Elements which will be loaded
    private final static String DATAFOLDERNAME = "data";
    private final List<AnalysisElement> analysisElements = new LinkedList<>();
    private final List<AnalysisExport> analysisExports = new LinkedList<>();
    private final List<AnalysisReport> analysisReports = new LinkedList<>();

    private final File dataDirectory;
    private final StorageSupplier storageSupplier;
    private final int size;
    private final AnalysisSummary analysisSummary;
    private final HtmlReport htmlReport;
    private final ColorScheme colorScheme;
    private final ChartTheme chartTheme;

    /** Constructor of the Analysis Class can be called with any combination of null and the respective parameter.
     * 
     * @param workingDirectory default: current working directory. Is the file where the config file, AmodeusOptions file and the outputfolder are located
     * @param configFile default: SimulationConfig file as defined in AmodeusOptions. Stores the data of the corresponding outputdirectory and Network.
     * @param outputDirectory: default: value stored in the Simulation Config file. Can be changed if for example an other outputfolder from the Sequential
     *            Server
     *            has to be analysed.
     * @param network: default: Network defined in the Config file. Can be used to reduce runtime if the Network was already loaded in a previous step (e.g.
     *            Scenario Server)
     * @throws Exception */

    private Analysis(File workingDirectory, File configFile, File outputDirectory, Network network) throws Exception {
        if (Objects.isNull(workingDirectory) || !workingDirectory.isDirectory())
            workingDirectory = new File("").getCanonicalFile();
        System.out.println("workingDirectory in Analysis: " + workingDirectory.getAbsolutePath());
        ScenarioOptions scenOptions = ScenarioOptions.load(workingDirectory);
        ReferenceFrame referenceFrame = scenOptions.getLocationSpec().referenceFrame();
        if (configFile == null || !configFile.isFile())
            configFile = new File(workingDirectory, scenOptions.getSimulationConfigName());
        if (outputDirectory == null || !outputDirectory.isDirectory()) {
            Config config = ConfigUtils.loadConfig(configFile.toString());
            String outputDirectoryName = config.controler().getOutputDirectory();
            outputDirectory = new File(workingDirectory, outputDirectoryName);
        }

        if (Objects.isNull(network)) {
            network = NetworkLoader.loadNetwork(configFile);
        }

        // load colorScheme & theme
        colorScheme = ColorScheme.valueOf(scenOptions.getColorScheme());
        chartTheme = ChartTheme.valueOf(scenOptions.getChartTheme());

        ChartFactory.setChartTheme(chartTheme.getChartTheme(false));
        BarRenderer.setDefaultBarPainter(new StandardBarPainter());
        BarRenderer.setDefaultShadowsVisible(false);

        outputDirectory.mkdir();
        dataDirectory = new File(outputDirectory, DATAFOLDERNAME);
        dataDirectory.mkdir();

        // load coordinate system
        MatsimStaticDatabase.initializeSingletonInstance(network, referenceFrame);

        // load simulation data
        StorageUtils storageUtils = new StorageUtils(outputDirectory);
        storageUtils.printStorageProperties();
        storageSupplier = new StorageSupplier(storageUtils.getFirstAvailableIteration());
        size = storageSupplier.size();
        System.out.println("Found files: " + size);
        int numVehicles = storageSupplier.getSimulationObject(1).vehicles.size();

        analysisSummary = new AnalysisSummary(numVehicles, size);

        // default List of Analysis Elements which will be loaded
        analysisElements.add(analysisSummary.getSimulationInformationElement());
        analysisElements.add(analysisSummary.getStatusDistribution());
        analysisElements.add(analysisSummary.getWaitingTimes());
        analysisElements.add(analysisSummary.getDistanceElement());

        analysisExports.add(new BinnedWaitingTimesImage());
        analysisExports.add(new DistanceDistributionOverDayImage());
        analysisExports.add(new OccupancyDistanceRatiosImage());
        analysisExports.add(new RequestsPerWaitingTimeImage());
        analysisExports.add(new StackedDistanceChartImage());
        analysisExports.add(new StatusDistributionImage());
        analysisExports.add(new ScenarioParametersExport());

        analysisExports.add(new DistancesOverDayTable());
        analysisExports.add(new DistancesRatiosTable());
        analysisExports.add(new WaitingTimesTable());
        analysisExports.add(new StatusDistributionTable());

        // default list of analysis reports
        htmlReport = new HtmlReport(configFile, outputDirectory, scenOptions);
        analysisReports.add(htmlReport);
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

    public void run() throws Exception {
        // Iteration over all Simulation Objects
        for (int index = 0; index < size; ++index) {
            SimulationObject simulationObject = storageSupplier.getSimulationObject(index);
            analysisElements.stream().forEach(analysisElement -> analysisElement.register(simulationObject));
            if (simulationObject.now % 10000 == 0)
                System.out.println(simulationObject.now);
        }

        // create plots and carry out other analysis on the data for each Analysis Element
        // TODO Find more effective way to give the relative Directory to the compile Function --> Jan
        analysisElements.forEach(AnalysisElement::consolidate);

        for (AnalysisExport analysisExport : analysisExports)
            analysisExport.summaryTarget(analysisSummary, dataDirectory, colorScheme);

        // Generate the Reports
        analysisReports.forEach(analysisReport -> analysisReport.generate(analysisSummary));
    }
}
