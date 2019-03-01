/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.TravelTimeAnalysis;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.io.TableBuilder;

// TODO add some code that explains the columns in the export folder
// TODO add units to export UnitExportUtils
public enum WaitingTimesTable implements AnalysisExport {
    INSTANCE;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
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
