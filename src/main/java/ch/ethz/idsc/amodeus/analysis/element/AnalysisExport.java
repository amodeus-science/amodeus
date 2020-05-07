/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

@FunctionalInterface
/** Functional interface that can be used to define (custom)
 * simulation analyses and how they are exported */
public interface AnalysisExport {
    /** @param analysisSummary
     * @param relativeDirectory relative directory for "output/001/data" where the simulation snapshots are stored
     * @param colorDataIndexed */
    void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed);
}
