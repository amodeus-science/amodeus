/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class HtmlReport implements AnalysisReport {
    private static final String REPORT_NAME = "report";
    private static final String TITLE = "AMoDeus Simulation Report";
    // ---
    private final File reportFolder;
    private final HtmlGenerator htmlGenerator;
    // Storage for all the Body Elements which are filld during Runntime by Analysis Elements
    private final Map<String, HtmlBodyElement> bodyElements = new LinkedHashMap<>();
    public final List<HtmlReportElement> htmlReportElements = new LinkedList<>();

    public HtmlReport(File outputdirectory, ScenarioOptions scenarioOptions) {
        reportFolder = new File(outputdirectory, REPORT_NAME + "/");
        reportFolder.mkdirs();
        if (!reportFolder.canWrite())
            throw new RuntimeException("The application does not have write access in the folder: \n" + reportFolder.getAbsolutePath());
        System.out.println("Generating AMoDeus report in: \n" + reportFolder.getAbsolutePath());
        GlobalAssert.that(reportFolder.isDirectory());

        /** copy the MATSim config file and the av.xml config file to the report folder of the simulation */
        try {
            saveConfigs(scenarioOptions);

        } catch (Exception ex) {
            System.err.println("Unable to copy MATSim config and av.xml config to report folder.");
            ex.printStackTrace();
        }
        htmlGenerator = new HtmlGenerator();
    }

    public void addHtmlReportElement(HtmlReportElement htmlReportElement) {
        htmlReportElements.add(htmlReportElement);
    }

    @Override
    public void generate(AnalysisSummary analysisSummary) {
        // Set up body elements
        for (HtmlReportElement htmlReportElement : htmlReportElements) {
            addBodyElements(htmlReportElement.process(analysisSummary));
        }

        /** setup **/
        htmlGenerator.html();
        htmlGenerator.insertCSS(//
                "h2 {float: left; width: 100%; padding: 10px}", //
                "pre {font-family: verdana; float: left; width: 100%; padding-left: 20px;}", //
                "p {float: left; width: 80%; padding-left: 20px;}", //
                "a {padding-left: 20px;}", //
                "#pre_left {float: left; width: 300px;}", "#pre_right {float: right; width: 300px;}", //
                "img {display: block; margin-left: auto; margin-right: auto;}" //
        );
        htmlGenerator.body();
        htmlGenerator.title(TITLE);

        /** report body elements **/
        for (Entry<String, HtmlBodyElement> entry : bodyElements.entrySet()) {
            htmlGenerator.insertSubTitle(entry.getKey());
            htmlGenerator.appendHTMLGenerator(entry.getValue().getHTMLGenerator());
        }

        /** report footer elements **/
        htmlGenerator.footer();
        htmlGenerator.insertLink("https://www.amodeus.science/", "www.amodeus.science");
        htmlGenerator.insertLink("http://www.idsc.ethz.ch/", "www.idsc.ethz.ch");
        htmlGenerator.footer();
        htmlGenerator.body();
        htmlGenerator.html();

        /** save report **/
        try {
            htmlGenerator.saveFile(REPORT_NAME, reportFolder);
            System.out.println("the report is located at " + reportFolder.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Not able to save report. ");
            e.printStackTrace();
        }

    }

    public void addBodyElements(Map<String, HtmlBodyElement> bodyElementsIn) {
        for (String bEK : bodyElementsIn.keySet()) {
            if (bodyElements.containsKey(bEK)) {
                bodyElements.get(bEK).append(bodyElementsIn.get(bEK));
            } else {
                bodyElements.put(bEK, bodyElementsIn.get(bEK));
            }
        }
    }

    private void saveConfigs(ScenarioOptions scenarioOptions) throws Exception {
        File configFile = new File(scenarioOptions.getSimulationConfigName());
        /** copy configFile */
        File configCopy = new File(reportFolder, configFile.getName());
        configCopy.delete();
        Files.copy(configFile.toPath(), configCopy.toPath());
        /** copy av.xml file */
        // TODO remove av.xml hardcode
        File avFile = new File(configFile.getParentFile(), "av.xml");
        File avCopy = new File(reportFolder, avFile.getName());
        avCopy.delete();
        Files.copy(avFile.toPath(), avCopy.toPath());
    }

}
