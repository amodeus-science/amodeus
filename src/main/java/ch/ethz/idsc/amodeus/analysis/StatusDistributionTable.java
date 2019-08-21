/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionElement;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.io.TableBuilder;

/* package */ enum StatusDistributionTable implements AnalysisExport {
    INSTANCE;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        StatusDistributionElement sd = analysisSummary.getStatusDistribution();

        TableBuilder tableBuilder = new TableBuilder();
        for (int index = 0; index < sd.time.length(); ++index)
            tableBuilder.appendRow(sd.time.Get(index), sd.statusTensor.get(index));

        try {
            SaveUtils.saveFile(tableBuilder.getTable(), "StatusDistribution", relativeDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
