/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;

@FunctionalInterface
public interface AnalysisReport {
    // TODO Joel document
    /** @param analysisSummary */
    void generate(AnalysisSummary analysisSummary);
}
