/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.io.TableBuilder;

public enum DistancesOverDayTable implements AnalysisExport {
    INSTANCE;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        DistanceElement de = analysisSummary.getDistanceElement();

        TableBuilder tableBuilder = new TableBuilder();
        for (int index = 0; index < de.time.length(); ++index)
            tableBuilder.appendRow(de.time.Get(index), de.distancesOverDay.get(index));

        try {
            SaveUtils.saveFile(tableBuilder.toTable(), "DistancesOverDay", relativeDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
