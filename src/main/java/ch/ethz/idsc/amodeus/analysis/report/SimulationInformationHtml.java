/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;

public enum SimulationInformationHtml implements HtmlReportElement {
    INSTANCE;

    @Override
    public Map<String, HtmlBodyElement> process(AnalysisSummary analysisSummary) {
        int size = analysisSummary.getSimulationInformationElement().vehicleSize();
        int reqsize = analysisSummary.getSimulationInformationElement().reqsize();
        // HTMLReport htmlReport = (HTMLReport) analysisReport;
        Map<String, HtmlBodyElement> bodyElements = new HashMap<>();
        HtmlBodyElement sIElement = new HtmlBodyElement();
        sIElement.getHTMLGenerator().insertTextLeft("Number of Vehicles:\n" //
                + "Number of Requests");
        sIElement.getHTMLGenerator().insertTextLeft(size + "\n" + reqsize);
        bodyElements.put(BodyElementKeys.SIMULATIONINFORMATION, sIElement);
        return bodyElements;
    }

}
