/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.report;

import java.util.Map;

import amodeus.amodeus.analysis.AnalysisSummary;

@FunctionalInterface
public interface HtmlReportElement {
    /** Function transforms information from the analysisSummary and from other sorces in the class to a Body Elements which are then appended to the Html
     * Report
     * Map Entries with the same Key will be combined under the same title afterwards.
     * 
     * @param analysisSummary
     * @return the Map with the Title of the Report Section as key and the HtmlBodyElement as value */
    Map<String, HtmlBodyElement> process(AnalysisSummary analysisSummary);
}
