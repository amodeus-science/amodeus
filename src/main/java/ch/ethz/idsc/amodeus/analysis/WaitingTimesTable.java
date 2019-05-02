/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.Quantiles;
import ch.ethz.idsc.amodeus.analysis.element.TravelTimeAnalysis;
import ch.ethz.idsc.amodeus.util.io.SaveFormats;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.io.TableBuilder;

/** The {@link WaitingTimesTable} stores a table for postprocessing which contains for each
 * time step 3 wait time quantiles and the mean wait time. */
/* package */ enum WaitingTimesTable implements AnalysisExport {
    INSTANCE;

    private final String identifier = "WaitingTimes";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        TravelTimeAnalysis tta = analysisSummary.getTravelTimeAnalysis();
        TableBuilder tableBuilder = new TableBuilder();
        for (int index = 0; index < tta.time.length(); ++index)
            tableBuilder.appendRow(tta.time.Get(index), tta.waitTimePlotValues.get(index));
        try {
            UnitSaveUtils.saveFile(tableBuilder.toTable(), identifier, relativeDirectory);
            File dataFolder = new File(relativeDirectory, identifier);
            SaveFormats.CSV.save(Tensors.fromString("time step, " + Quantiles.LBL[0] + ", " //
                    + Quantiles.LBL[1] + ", " + Quantiles.LBL[2] + ", mean wait time"), //
                    dataFolder, "description");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
