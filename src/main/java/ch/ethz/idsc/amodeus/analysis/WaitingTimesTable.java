/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.TravelTimeAnalysis;
import ch.ethz.idsc.amodeus.analysis.plot.ColorScheme;
import ch.ethz.idsc.tensor.io.TableBuilder;

public enum WaitingTimesTable implements AnalysisExport {
    INSTANCE;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorScheme colorScheme) {
        TravelTimeAnalysis tta = analysisSummary.getTravelTimeAnalysis();
        TableBuilder tableBuilder = new TableBuilder();
        for (int index = 0; index < tta.time.length(); ++index)
            tableBuilder.appendRow(tta.time.Get(index), tta.waitTimePlotValues.get(index));

        try {
            SaveUtils.saveFile(tableBuilder.toTable(), "WaitingTimes", relativeDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
