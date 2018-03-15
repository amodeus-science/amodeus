/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.WaitingTimesElement;
import ch.ethz.idsc.tensor.io.TableBuilder;

public class WaitingTimesTable implements AnalysisExport {

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory) {
        WaitingTimesElement wt = analysisSummary.getWaitingTimes();

        TableBuilder tableBuilder = new TableBuilder();
        for (int index = 0; index < wt.time.length(); ++index)
            tableBuilder.appendRow(wt.time.Get(index), wt.waitTimePlotValues.get(index));

        try {
            SaveUtils.saveFile(tableBuilder.toTable(), "WaitingTimes", relativeDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
