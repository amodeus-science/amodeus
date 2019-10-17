/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.io.TableBuilder;

/* package */ enum DistancesRatiosTable implements AnalysisExport {
    INSTANCE;

    private static final String IDENTIFIER = "DistanceRatios";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        DistanceElement distanceElement = analysisSummary.getDistanceElement();

        TableBuilder tableBuilder = new TableBuilder();
        for (int index = 0; index < distanceElement.time.length(); ++index)
            tableBuilder.appendRow(distanceElement.time.Get(index), distanceElement.ratios.get(index));

        try {
            SaveUtils.saveFile(tableBuilder.getTable(), IDENTIFIER, relativeDirectory);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
