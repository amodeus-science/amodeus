/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionElement;
import ch.ethz.idsc.tensor.io.TableBuilder;

public class StatusDistributionTable implements AnalysisExport {

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory) {
        StatusDistributionElement sd = analysisSummary.getStatusDistribution();

        TableBuilder tableBuilder = new TableBuilder();
        for (int index = 0; index < sd.time.length(); ++index)
            tableBuilder.appendRow(sd.time.Get(index), sd.statusTensor.get(index));

        try {
            SaveUtils.saveFile(tableBuilder.toTable(), "StatusDistribution", relativeDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
