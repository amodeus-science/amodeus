/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.plot.ColorScheme;

public interface AnalysisExport {
    /** @param analysisSummary
     * @param relativeDirectory for instance "output/001/data" */
    void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorScheme colorScheme);
}
