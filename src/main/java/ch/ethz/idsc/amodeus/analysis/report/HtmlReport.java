/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.io.File;
import java.io.IOException;
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

    public HtmlReport(File configFile, File outputdirectory, ScenarioOptions scenarioOptions) {
        reportFolder = new File(outputdirectory, REPORT_NAME);
        reportFolder.mkdir();
        GlobalAssert.that(reportFolder.isDirectory());

        // extract necessary data
        try {
            saveConfigs(configFile, scenarioOptions);
        } catch (IOException e1) {
            System.err.println("Scenario Parameters could not be found");
            e1.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error while coping the Config file into the report folder");
            e.printStackTrace();
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

    private void saveConfigs(File configFile, ScenarioOptions scenarioOptions) throws Exception {
        { // copy configFile
            File dest = new File(reportFolder, scenarioOptions.getSimulationConfigName());
            dest.delete();
            Files.copy(configFile.toPath(), dest.toPath());
        }
        { // copy av.xml file
            File dest = new File(reportFolder, "av.xml");
            dest.delete();
            File avFile = new File(configFile.getParentFile(), "av.xml");
            Files.copy(avFile.toPath(), dest.toPath());
        }
    }

}
