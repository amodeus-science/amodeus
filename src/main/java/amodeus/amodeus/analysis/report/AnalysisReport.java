/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.report;

import amodeus.amodeus.analysis.AnalysisSummary;

@FunctionalInterface
public interface AnalysisReport {
    /** @param analysisSummary */
    void generate(AnalysisSummary analysisSummary);
}
